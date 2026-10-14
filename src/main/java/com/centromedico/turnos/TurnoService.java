package com.centromedico.turnos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.centromedico.config.RecursoNoEncontradoException;
import com.centromedico.config.ReglaNegocioException;
import com.centromedico.pacientes.Paciente;
import com.centromedico.pacientes.PacienteRepository;
import com.centromedico.profesionales.Profesional;
import com.centromedico.profesionales.ProfesionalRepository;

@Service
public class TurnoService {

    private static final List<EstadoTurno> ACTIVOS = List.of(EstadoTurno.RESERVADO, EstadoTurno.EN_ESPERA);
    private static final List<String> DIAS = List.of("LUN", "MAR", "MIE", "JUE", "VIE", "SAB", "DOM");
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final TurnoRepository repository;
    private final PacienteRepository pacienteRepository;
    private final ProfesionalRepository profesionalRepository;

    public TurnoService(TurnoRepository repository, PacienteRepository pacienteRepository,
            ProfesionalRepository profesionalRepository) {
        this.repository = repository;
        this.pacienteRepository = pacienteRepository;
        this.profesionalRepository = profesionalRepository;
    }

    // ------------------------------------------------------------------ consultas

    @Transactional(readOnly = true)
    public List<Turno> listar(LocalDate fecha, EstadoTurno estado, Long pacienteId, Long profesionalId) {
        return repository.filtrar(fecha, estado, pacienteId, profesionalId);
    }

    @Transactional(readOnly = true)
    public Turno obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Turno", id));
    }

    /** Agenda del profesional para un día: todos los turnos, en orden horario. */
    @Transactional(readOnly = true)
    public List<Turno> agenda(Long profesionalId, LocalDate fecha) {
        buscarProfesional(profesionalId);
        return repository.findByProfesionalIdAndFechaOrderByHoraAsc(profesionalId, fecha);
    }

    /** Horarios ("HH:mm") todavía libres de un profesional en una fecha. Alimenta el desplegable del formulario. */
    @Transactional(readOnly = true)
    public List<String> horariosLibres(Long profesionalId, LocalDate fecha) {
        Profesional pro = buscarProfesional(profesionalId);
        List<String> libres = new ArrayList<>();
        if (!pro.isActivo() || !pro.getEspecialidad().isActiva() || fecha.isBefore(LocalDate.now())
                || !atiendeEseDia(pro, fecha)) {
            return libres;
        }
        int dur = pro.getEspecialidad().getDuracionTurnoMin();
        int desde = minutos(pro.getHoraDesde());
        int hasta = minutos(pro.getHoraHasta());
        List<Turno> ocupados = repository.findByProfesionalIdAndFechaAndEstadoIn(profesionalId, fecha, ACTIVOS);
        LocalTime ahora = LocalTime.now();
        for (int inicio = desde; inicio + dur <= hasta; inicio += dur) {
            LocalTime h = LocalTime.ofSecondOfDay(inicio * 60L);
            if (fecha.equals(LocalDate.now()) && h.isBefore(ahora)) {
                continue;
            }
            final int ini = inicio;
            boolean ocupado = ocupados.stream()
                    .anyMatch(t -> solapan(ini, ini + dur, minutos(t.getHora()), minutos(t.getHora()) + dur));
            if (!ocupado) {
                libres.add(h.format(HH_MM));
            }
        }
        return libres;
    }

    // ------------------------------------------------------------------ alta y modificación

    @Transactional
    public Turno crear(Turno t, String usuario) {
        Paciente paciente = pacienteRepository.findById(t.getPaciente().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente", t.getPaciente().getId()));
        Profesional profesional = buscarProfesional(t.getProfesional().getId());
        validarActivos(paciente, profesional);
        validarDisponibilidad(profesional, t.getFecha(), t.getHora(), null, paciente.getId());

        t.setId(null);
        t.setPaciente(paciente);
        t.setProfesional(profesional);
        t.setEstado(EstadoTurno.RESERVADO);
        t.setCreadoEn(LocalDateTime.now());
        t.setCreadoPor(usuario);
        return repository.save(t);
    }

    /** Reprogramar: cambia profesional, fecha, hora y notas de un turno que todavía está RESERVADO. */
    @Transactional
    public Turno actualizar(Long id, Turno datos) {
        Turno actual = obtener(id);
        if (actual.getEstado() != EstadoTurno.RESERVADO) {
            throw new ReglaNegocioException("Solo se pueden modificar turnos en estado RESERVADO");
        }
        Profesional profesional = buscarProfesional(datos.getProfesional().getId());
        validarActivos(actual.getPaciente(), profesional);
        validarDisponibilidad(profesional, datos.getFecha(), datos.getHora(), id, actual.getPaciente().getId());

        actual.setProfesional(profesional);
        actual.setFecha(datos.getFecha());
        actual.setHora(datos.getHora());
        actual.setMotivo(datos.getMotivo());
        actual.setObservaciones(datos.getObservaciones());
        return repository.save(actual);
    }

    // ------------------------------------------------------------------ estados

    @Transactional
    public Turno cambiarEstado(Long id, EstadoTurno nuevo) {
        Turno t = obtener(id);
        if (nuevo == EstadoTurno.ATENDIDO) {
            throw new ReglaNegocioException(
                    "El turno pasa a ATENDIDO cuando el profesional registra la atención");
        }
        if (!t.getEstado().puedePasarA(nuevo)) {
            throw new ReglaNegocioException("Un turno " + t.getEstado() + " no puede pasar a " + nuevo);
        }
        if (nuevo == EstadoTurno.EN_ESPERA && !t.getFecha().equals(LocalDate.now())) {
            throw new ReglaNegocioException("La llegada del paciente solo se registra el día del turno");
        }
        if (nuevo == EstadoTurno.AUSENTE && LocalDateTime.now().isBefore(LocalDateTime.of(t.getFecha(), t.getHora()))) {
            throw new ReglaNegocioException("Solo se puede marcar AUSENTE un turno cuyo horario ya comenzó");
        }
        t.setEstado(nuevo);
        return repository.save(t);
    }

    @Transactional
    public void cancelar(Long id) {
        cambiarEstado(id, EstadoTurno.CANCELADO);
    }

    // ------------------------------------------------------------------ reglas de negocio

    /** RN-05: paciente, profesional y especialidad deben estar activos. */
    private void validarActivos(Paciente paciente, Profesional profesional) {
        if (!paciente.isActivo()) {
            throw new ReglaNegocioException("El paciente está dado de baja y no puede sacar turnos");
        }
        if (!profesional.isActivo() || !profesional.getEspecialidad().isActiva()) {
            throw new ReglaNegocioException("El profesional o su especialidad no están disponibles");
        }
    }

    /** RN-01 a RN-04. Si se está reprogramando, excluirId es el turno que se está moviendo. */
    private void validarDisponibilidad(Profesional pro, LocalDate fecha, LocalTime hora, Long excluirId,
            Long pacienteId) {
        // RN-03: nada en el pasado
        if (LocalDateTime.of(fecha, hora).isBefore(LocalDateTime.now())) {
            throw new ReglaNegocioException("No se pueden reservar turnos en una fecha u hora pasada");
        }
        // RN-02: día y franja de atención + grilla de la especialidad
        if (!atiendeEseDia(pro, fecha)) {
            throw new ReglaNegocioException("Dr/a. " + pro.getApellido() + " no atiende los "
                    + DIAS.get(fecha.getDayOfWeek().getValue() - 1) + " (atiende: " + pro.getDiasAtencion() + ")");
        }
        int dur = pro.getEspecialidad().getDuracionTurnoMin();
        int inicio = minutos(hora);
        int fin = inicio + dur;
        int desde = minutos(pro.getHoraDesde());
        int hasta = minutos(pro.getHoraHasta());
        if (inicio < desde || fin > hasta) {
            throw new ReglaNegocioException("El horario está fuera de la atención del profesional ("
                    + pro.getHoraDesde().format(HH_MM) + " a " + pro.getHoraHasta().format(HH_MM) + ")");
        }
        if ((inicio - desde) % dur != 0) {
            throw new ReglaNegocioException("Los turnos de " + pro.getEspecialidad().getNombre() + " se dan cada "
                    + dur + " minutos a partir de las " + pro.getHoraDesde().format(HH_MM));
        }
        // RN-01: el profesional no puede tener dos turnos activos superpuestos
        for (Turno otro : repository.findByProfesionalIdAndFechaAndEstadoIn(pro.getId(), fecha, ACTIVOS)) {
            if (!otro.getId().equals(excluirId)
                    && solapan(inicio, fin, minutos(otro.getHora()), minutos(otro.getHora()) + dur)) {
                throw new ReglaNegocioException("El profesional ya tiene un turno a las "
                        + otro.getHora().format(HH_MM) + " que se superpone con ese horario");
            }
        }
        // RN-04: el paciente tampoco puede estar en dos consultorios a la vez
        for (Turno otro : repository.findByPacienteIdAndFechaAndEstadoIn(pacienteId, fecha, ACTIVOS)) {
            int durOtro = otro.getProfesional().getEspecialidad().getDuracionTurnoMin();
            if (!otro.getId().equals(excluirId)
                    && solapan(inicio, fin, minutos(otro.getHora()), minutos(otro.getHora()) + durOtro)) {
                throw new ReglaNegocioException("El paciente ya tiene un turno ese día a las "
                        + otro.getHora().format(HH_MM) + " con Dr/a. " + otro.getProfesional().getApellido());
            }
        }
    }

    private boolean atiendeEseDia(Profesional pro, LocalDate fecha) {
        String dia = DIAS.get(fecha.getDayOfWeek().getValue() - 1);
        return Arrays.asList(pro.getDiasAtencion().split(",")).contains(dia);
    }

    private boolean solapan(int inicioA, int finA, int inicioB, int finB) {
        return inicioA < finB && inicioB < finA;
    }

    private int minutos(LocalTime h) {
        return h.toSecondOfDay() / 60;
    }

    private Profesional buscarProfesional(Long id) {
        return profesionalRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Profesional", id));
    }
}

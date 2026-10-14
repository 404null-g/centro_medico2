package com.centromedico.atenciones;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.centromedico.config.RecursoNoEncontradoException;
import com.centromedico.config.ReglaNegocioException;
import com.centromedico.pacientes.PacienteRepository;
import com.centromedico.turnos.EstadoTurno;
import com.centromedico.turnos.Turno;
import com.centromedico.turnos.TurnoRepository;
import com.centromedico.usuarios.Rol;
import com.centromedico.usuarios.Usuario;

@Service
public class AtencionService {

    private static final long HORAS_PARA_CORREGIR = 24;

    private final AtencionRepository repository;
    private final TurnoRepository turnoRepository;
    private final PacienteRepository pacienteRepository;

    public AtencionService(AtencionRepository repository, TurnoRepository turnoRepository,
            PacienteRepository pacienteRepository) {
        this.repository = repository;
        this.turnoRepository = turnoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    /** Atenciones registradas por el profesional que inició sesión. */
    @Transactional(readOnly = true)
    public List<Atencion> listarPropias(Usuario usuario) {
        return repository.findByTurnoProfesionalIdOrderByFechaHoraDesc(profesionalDe(usuario));
    }

    @Transactional(readOnly = true)
    public Atencion obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Atención", id));
    }

    /** RN-10: cualquier profesional puede consultar la historia clínica de un paciente. */
    @Transactional(readOnly = true)
    public List<Atencion> historiaClinica(Long pacienteId) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new RecursoNoEncontradoException("Paciente", pacienteId);
        }
        return repository.findByTurnoPacienteIdOrderByFechaHoraDesc(pacienteId);
    }

    /** RN-07: registra la atención y pasa el turno a ATENDIDO en la misma transacción. */
    @Transactional
    public Atencion registrar(Atencion a, Usuario usuario) {
        Long profesionalId = profesionalDe(usuario);
        Turno turno = turnoRepository.findById(a.getTurno().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Turno", a.getTurno().getId()));

        if (!turno.getProfesional().getId().equals(profesionalId)) {
            throw new AccessDeniedException("Solo el profesional del turno puede registrar la atención");
        }
        if (turno.getEstado() != EstadoTurno.EN_ESPERA) {
            throw new ReglaNegocioException(
                    "Solo se puede atender a un paciente que figure EN_ESPERA (estado actual: " + turno.getEstado() + ")");
        }
        if (repository.existsByTurnoId(turno.getId())) {
            throw new ReglaNegocioException("Ese turno ya tiene una atención registrada");
        }

        a.setId(null);
        a.setTurno(turno);
        a.setFechaHora(LocalDateTime.now());
        a.setRegistradaPor(usuario.getUsername());
        turno.setEstado(EstadoTurno.ATENDIDO);
        turnoRepository.save(turno);
        return repository.save(a);
    }

    /** RN-08: solo el autor puede corregir su atención, y dentro de las 24 horas. */
    @Transactional
    public Atencion actualizar(Long id, Atencion datos, Usuario usuario) {
        profesionalDe(usuario);
        Atencion actual = obtener(id);
        if (!actual.getRegistradaPor().equals(usuario.getUsername())) {
            throw new AccessDeniedException("Solo quien registró la atención puede corregirla");
        }
        if (Duration.between(actual.getFechaHora(), LocalDateTime.now()).toHours() >= HORAS_PARA_CORREGIR) {
            throw new ReglaNegocioException(
                    "Pasaron más de " + HORAS_PARA_CORREGIR + " horas: la atención ya no puede modificarse");
        }
        actual.setMotivoConsulta(datos.getMotivoConsulta());
        actual.setDiagnostico(datos.getDiagnostico());
        actual.setIndicaciones(datos.getIndicaciones());
        actual.setObservaciones(datos.getObservaciones());
        return repository.save(actual);
    }

    private Long profesionalDe(Usuario usuario) {
        if (usuario.getRol() != Rol.PROFESIONAL || usuario.getProfesional() == null) {
            throw new AccessDeniedException("Solo los profesionales trabajan con atenciones");
        }
        return usuario.getProfesional().getId();
    }
}

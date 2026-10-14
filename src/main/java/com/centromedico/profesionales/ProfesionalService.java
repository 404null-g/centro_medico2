package com.centromedico.profesionales;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.centromedico.config.RecursoNoEncontradoException;
import com.centromedico.config.ReglaNegocioException;
import com.centromedico.especialidades.Especialidad;
import com.centromedico.especialidades.EspecialidadRepository;

@Service
public class ProfesionalService {

    private final ProfesionalRepository repository;
    private final EspecialidadRepository especialidadRepository;

    public ProfesionalService(ProfesionalRepository repository, EspecialidadRepository especialidadRepository) {
        this.repository = repository;
        this.especialidadRepository = especialidadRepository;
    }

    @Transactional(readOnly = true)
    public List<Profesional> listar(String q, Long especialidadId, boolean soloActivos) {
        String filtro = (q == null || q.isBlank()) ? null : q.trim();
        return repository.filtrar(filtro, especialidadId, soloActivos);
    }

    @Transactional(readOnly = true)
    public Profesional obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Profesional", id));
    }

    @Transactional
    public Profesional crear(Profesional p) {
        p.setId(null);
        normalizar(p);
        validarReglas(p);
        if (repository.existsByMatricula(p.getMatricula())) {
            throw new ReglaNegocioException("Ya existe un profesional con la matrícula " + p.getMatricula());
        }
        if (repository.existsByDni(p.getDni())) {
            throw new ReglaNegocioException("Ya existe un profesional con el DNI " + p.getDni());
        }
        p.setActivo(true);
        return repository.save(p);
    }

    @Transactional
    public Profesional actualizar(Long id, Profesional datos) {
        Profesional actual = obtener(id);
        normalizar(datos);
        validarReglas(datos);
        if (repository.existsByMatriculaAndIdNot(datos.getMatricula(), id)) {
            throw new ReglaNegocioException("Ya existe otro profesional con la matrícula " + datos.getMatricula());
        }
        if (repository.existsByDniAndIdNot(datos.getDni(), id)) {
            throw new ReglaNegocioException("Ya existe otro profesional con el DNI " + datos.getDni());
        }
        actual.setMatricula(datos.getMatricula());
        actual.setDni(datos.getDni());
        actual.setApellido(datos.getApellido());
        actual.setNombre(datos.getNombre());
        actual.setTelefono(datos.getTelefono());
        actual.setEmail(datos.getEmail());
        actual.setConsultorio(datos.getConsultorio());
        actual.setDiasAtencion(datos.getDiasAtencion());
        actual.setHoraDesde(datos.getHoraDesde());
        actual.setHoraHasta(datos.getHoraHasta());
        actual.setActivo(datos.isActivo());
        actual.setEspecialidad(datos.getEspecialidad());
        return repository.save(actual);
    }

    /** Baja lógica: el profesional deja de aparecer para nuevos turnos; sus atenciones se conservan. */
    @Transactional
    public void darDeBaja(Long id) {
        Profesional p = obtener(id);
        p.setActivo(false);
        repository.save(p);
    }

    // ---------------------------------------------------------------- reglas

    private void normalizar(Profesional p) {
        p.setMatricula(p.getMatricula().trim().toUpperCase());
        p.setApellido(p.getApellido().trim());
        p.setNombre(p.getNombre().trim());
        p.setConsultorio(p.getConsultorio().trim());
        p.setDiasAtencion(p.getDiasAtencion().replaceAll("\\s", "").toUpperCase());
        if (p.getTelefono() != null && p.getTelefono().isBlank()) p.setTelefono(null);
        if (p.getEmail() != null && p.getEmail().isBlank()) p.setEmail(null);
    }

    /** RN-12: franja horaria coherente y especialidad existente y activa. */
    private void validarReglas(Profesional p) {
        if (!p.getHoraDesde().isBefore(p.getHoraHasta())) {
            throw new ReglaNegocioException("La hora de inicio debe ser anterior a la hora de fin");
        }
        Especialidad e = especialidadRepository.findById(p.getEspecialidad().getId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Especialidad", p.getEspecialidad().getId()));
        if (!e.isActiva()) {
            throw new ReglaNegocioException("La especialidad \"" + e.getNombre() + "\" está dada de baja");
        }
        long minutos = java.time.Duration.between(p.getHoraDesde(), p.getHoraHasta()).toMinutes();
        if (minutos < e.getDuracionTurnoMin()) {
            throw new ReglaNegocioException("La franja horaria es menor a la duración de un turno ("
                    + e.getDuracionTurnoMin() + " min)");
        }
        p.setEspecialidad(e);
    }
}

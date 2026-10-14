package com.centromedico.especialidades;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.centromedico.config.RecursoNoEncontradoException;
import com.centromedico.config.ReglaNegocioException;
import com.centromedico.profesionales.ProfesionalRepository;

@Service
public class EspecialidadService {

    private final EspecialidadRepository repository;
    private final ProfesionalRepository profesionalRepository;

    public EspecialidadService(EspecialidadRepository repository, ProfesionalRepository profesionalRepository) {
        this.repository = repository;
        this.profesionalRepository = profesionalRepository;
    }

    @Transactional(readOnly = true)
    public List<Especialidad> listar(boolean soloActivas) {
        return soloActivas ? repository.findByActivaTrueOrderByNombreAsc() : repository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Especialidad obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Especialidad", id));
    }

    @Transactional
    public Especialidad crear(Especialidad e) {
        e.setId(null);
        e.setNombre(e.getNombre().trim());
        if (repository.existsByNombreIgnoreCase(e.getNombre())) {
            throw new ReglaNegocioException("Ya existe la especialidad \"" + e.getNombre() + "\"");
        }
        e.setActiva(true);
        return repository.save(e);
    }

    @Transactional
    public Especialidad actualizar(Long id, Especialidad datos) {
        Especialidad actual = obtener(id);
        datos.setNombre(datos.getNombre().trim());
        if (repository.existsByNombreIgnoreCaseAndIdNot(datos.getNombre(), id)) {
            throw new ReglaNegocioException("Ya existe otra especialidad llamada \"" + datos.getNombre() + "\"");
        }
        if (!datos.isActiva()) {
            validarSinProfesionalesActivos(id);
        }
        actual.setNombre(datos.getNombre());
        actual.setDescripcion(datos.getDescripcion());
        actual.setDuracionTurnoMin(datos.getDuracionTurnoMin());
        actual.setActiva(datos.isActiva());
        return repository.save(actual);
    }

    /** RN-12: no se puede dar de baja una especialidad que todavía tiene profesionales activos. */
    @Transactional
    public void darDeBaja(Long id) {
        Especialidad e = obtener(id);
        validarSinProfesionalesActivos(id);
        e.setActiva(false);
        repository.save(e);
    }

    private void validarSinProfesionalesActivos(Long especialidadId) {
        if (profesionalRepository.existsByEspecialidadIdAndActivoTrue(especialidadId)) {
            throw new ReglaNegocioException(
                    "No se puede dar de baja: hay profesionales activos con esta especialidad");
        }
    }
}

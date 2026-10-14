package com.centromedico.pacientes;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.centromedico.config.RecursoNoEncontradoException;
import com.centromedico.config.ReglaNegocioException;

@Service
public class PacienteService {

    private final PacienteRepository repository;

    public PacienteService(PacienteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Paciente> listar(String q) {
        if (q == null || q.isBlank()) {
            return repository.findAllByOrderByApellidoAscNombreAsc();
        }
        return repository.buscar(q.trim());
    }

    @Transactional(readOnly = true)
    public Paciente obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente", id));
    }

    @Transactional
    public Paciente crear(Paciente p) {
        p.setId(null);
        normalizar(p);
        validarReglas(p);
        if (repository.existsByDni(p.getDni())) {
            throw new ReglaNegocioException("Ya existe un paciente registrado con el DNI " + p.getDni());
        }
        p.setActivo(true);
        return repository.save(p);
    }

    @Transactional
    public Paciente actualizar(Long id, Paciente datos) {
        Paciente actual = obtener(id);
        normalizar(datos);
        validarReglas(datos);
        if (repository.existsByDniAndIdNot(datos.getDni(), id)) {
            throw new ReglaNegocioException("Ya existe otro paciente con el DNI " + datos.getDni());
        }
        actual.setDni(datos.getDni());
        actual.setApellido(datos.getApellido());
        actual.setNombre(datos.getNombre());
        actual.setFechaNacimiento(datos.getFechaNacimiento());
        actual.setSexo(datos.getSexo());
        actual.setTelefono(datos.getTelefono());
        actual.setEmail(datos.getEmail());
        actual.setDomicilio(datos.getDomicilio());
        actual.setLocalidad(datos.getLocalidad());
        actual.setObraSocial(datos.getObraSocial());
        actual.setNroAfiliado(datos.getNroAfiliado());
        actual.setActivo(datos.isActivo());
        return repository.save(actual);
    }

    /** Baja lógica: el paciente deja de poder sacar turnos pero conserva su historia clínica. */
    @Transactional
    public void darDeBaja(Long id) {
        Paciente p = obtener(id);
        p.setActivo(false);
        repository.save(p);
    }

    // ---------------------------------------------------------------- reglas

    private void normalizar(Paciente p) {
        p.setDni(p.getDni() == null ? null : p.getDni().replaceAll("[.\\s]", ""));
        p.setApellido(limpiar(p.getApellido()));
        p.setNombre(limpiar(p.getNombre()));
        p.setObraSocial(vacioANull(p.getObraSocial()));
        p.setNroAfiliado(vacioANull(p.getNroAfiliado()));
        p.setTelefono(vacioANull(p.getTelefono()));
        p.setEmail(vacioANull(p.getEmail()));
    }

    /** RN-09: con obra social es obligatorio el nº de afiliado; el afiliado sin obra social no tiene sentido. */
    private void validarReglas(Paciente p) {
        boolean conObraSocial = p.getObraSocial() != null;
        boolean conAfiliado = p.getNroAfiliado() != null;
        if (conObraSocial && !conAfiliado) {
            throw new ReglaNegocioException("Si el paciente tiene obra social debe informar el número de afiliado");
        }
        if (!conObraSocial && conAfiliado) {
            throw new ReglaNegocioException("Se cargó un número de afiliado pero no la obra social");
        }
    }

    private String limpiar(String s) {
        return s == null ? null : s.trim().replaceAll("\\s+", " ");
    }

    private String vacioANull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}

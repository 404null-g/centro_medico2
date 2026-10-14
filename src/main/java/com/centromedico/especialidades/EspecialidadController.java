package com.centromedico.especialidades;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadController {

    private final EspecialidadService service;

    public EspecialidadController(EspecialidadService service) {
        this.service = service;
    }

    /** GET /api/especialidades  |  GET /api/especialidades?soloActivas=true (para los desplegables) */
    @GetMapping
    public List<Especialidad> listar(@RequestParam(defaultValue = "false") boolean soloActivas) {
        return service.listar(soloActivas);
    }

    @GetMapping("/{id}")
    public Especialidad obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Especialidad crear(@Valid @RequestBody Especialidad especialidad) {
        return service.crear(especialidad);
    }

    @PutMapping("/{id}")
    public Especialidad actualizar(@PathVariable Long id, @Valid @RequestBody Especialidad especialidad) {
        return service.actualizar(id, especialidad);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(@PathVariable Long id) {
        service.darDeBaja(id);
    }
}

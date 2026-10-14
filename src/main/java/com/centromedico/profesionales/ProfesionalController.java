package com.centromedico.profesionales;

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
@RequestMapping("/api/profesionales")
public class ProfesionalController {

    private final ProfesionalService service;

    public ProfesionalController(ProfesionalService service) {
        this.service = service;
    }

    /** GET /api/profesionales?q=lopez&especialidadId=3&soloActivos=true */
    @GetMapping
    public List<Profesional> listar(@RequestParam(required = false) String q,
            @RequestParam(required = false) Long especialidadId,
            @RequestParam(defaultValue = "false") boolean soloActivos) {
        return service.listar(q, especialidadId, soloActivos);
    }

    @GetMapping("/{id}")
    public Profesional obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Profesional crear(@Valid @RequestBody Profesional profesional) {
        return service.crear(profesional);
    }

    @PutMapping("/{id}")
    public Profesional actualizar(@PathVariable Long id, @Valid @RequestBody Profesional profesional) {
        return service.actualizar(id, profesional);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void darDeBaja(@PathVariable Long id) {
        service.darDeBaja(id);
    }
}

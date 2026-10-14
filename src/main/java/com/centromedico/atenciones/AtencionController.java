package com.centromedico.atenciones;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.centromedico.usuarios.UsuarioService;

import jakarta.validation.Valid;

/** Acceso restringido al rol PROFESIONAL (ver SecurityConfig). */
@RestController
public class AtencionController {

    private final AtencionService service;
    private final UsuarioService usuarioService;

    public AtencionController(AtencionService service, UsuarioService usuarioService) {
        this.service = service;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/api/atenciones")
    public List<Atencion> listarPropias(Authentication auth) {
        return service.listarPropias(usuarioService.porUsername(auth.getName()));
    }

    @GetMapping("/api/atenciones/{id}")
    public Atencion obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping("/api/atenciones")
    @ResponseStatus(HttpStatus.CREATED)
    public Atencion registrar(@Valid @RequestBody Atencion atencion, Authentication auth) {
        return service.registrar(atencion, usuarioService.porUsername(auth.getName()));
    }

    @PutMapping("/api/atenciones/{id}")
    public Atencion actualizar(@PathVariable Long id, @Valid @RequestBody Atencion atencion, Authentication auth) {
        return service.actualizar(id, atencion, usuarioService.porUsername(auth.getName()));
    }

    /** Historia clínica: GET /api/pacientes/{id}/atenciones */
    @GetMapping("/api/pacientes/{id}/atenciones")
    public List<Atencion> historiaClinica(@PathVariable Long id) {
        return service.historiaClinica(id);
    }
}

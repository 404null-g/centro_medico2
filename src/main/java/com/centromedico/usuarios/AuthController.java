package com.centromedico.usuarios;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/auth/login  y  POST /api/auth/logout los resuelve Spring Security (ver SecurityConfig).
 * Este controlador solo agrega GET /api/auth/me para que el frontend sepa quién está logueado
 * y qué menú mostrar.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        Usuario u = usuarioService.porUsername(auth.getName());
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("username", u.getUsername());
        datos.put("rol", u.getRol().name());
        if (u.getProfesional() != null) {
            datos.put("profesionalId", u.getProfesional().getId());
            datos.put("nombre", u.getProfesional().getApellido() + ", " + u.getProfesional().getNombre());
        } else {
            datos.put("nombre", u.getUsername());
        }
        return datos;
    }
}

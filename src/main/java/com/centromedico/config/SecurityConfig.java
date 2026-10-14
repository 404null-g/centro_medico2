package com.centromedico.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Autenticación con sesión (cookie JSESSIONID) y autorización por rol.
 * Resumen de permisos (la matriz completa está en la documentación):
 *   ADMIN       -> todo, excepto las historias clínicas
 *   RECEPCION   -> pacientes, turnos y consulta de profesionales
 *   PROFESIONAL -> su agenda, sus atenciones y las historias clínicas
 * Las reglas se evalúan en orden: la primera que coincide es la que se aplica.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Simplificación académica: la API se consume desde el mismo origen con fetch().
            // En producción habría que habilitar CSRF o usar tokens.
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // --- páginas y recursos estáticos (la protección real está en la API)
                .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/img/**", "/favicon.ico", "/error").permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/**").authenticated()

                // --- usuarios
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")

                // --- historia clínica y atenciones: solo profesionales (confidencialidad)
                .requestMatchers("/api/atenciones/**").hasRole("PROFESIONAL")
                .requestMatchers(HttpMethod.GET, "/api/pacientes/*/atenciones").hasRole("PROFESIONAL")

                // --- pacientes
                .requestMatchers(HttpMethod.GET, "/api/pacientes/**").hasAnyRole("ADMIN", "RECEPCION", "PROFESIONAL")
                .requestMatchers("/api/pacientes/**").hasAnyRole("ADMIN", "RECEPCION")

                // --- profesionales y especialidades (incluye agenda y horarios libres)
                .requestMatchers(HttpMethod.GET, "/api/profesionales/**", "/api/especialidades/**")
                        .hasAnyRole("ADMIN", "RECEPCION", "PROFESIONAL")
                .requestMatchers("/api/profesionales/**", "/api/especialidades/**").hasRole("ADMIN")

                // --- turnos
                .requestMatchers(HttpMethod.GET, "/api/turnos/**").hasAnyRole("ADMIN", "RECEPCION", "PROFESIONAL")
                .requestMatchers("/api/turnos/**").hasAnyRole("ADMIN", "RECEPCION")

                .anyRequest().authenticated())
            // POST /api/auth/login con los campos username y password (formulario)
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .successHandler((req, res, auth) -> {
                    res.setStatus(HttpStatus.OK.value());
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"status\":200,\"mensaje\":\"Sesión iniciada\"}");
                })
                .failureHandler((req, res, ex) -> {
                    res.setStatus(HttpStatus.UNAUTHORIZED.value());
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"status\":401,\"mensaje\":\"Usuario o contraseña incorrectos, o usuario dado de baja\"}");
                })
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpStatus.OK.value())))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(HttpStatus.FORBIDDEN.value());
                    res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"status\":403,\"mensaje\":\"No tiene permisos para realizar esta acción\"}");
                }));
        return http.build();
    }
}

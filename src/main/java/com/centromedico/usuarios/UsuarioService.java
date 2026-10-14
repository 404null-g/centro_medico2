package com.centromedico.usuarios;

import java.util.List;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.centromedico.config.RecursoNoEncontradoException;
import com.centromedico.config.ReglaNegocioException;
import com.centromedico.profesionales.Profesional;
import com.centromedico.profesionales.ProfesionalRepository;

/**
 * Gestiona los usuarios y, además, le dice a Spring Security cómo buscar a quien intenta iniciar sesión.
 */
@Service
public class UsuarioService implements UserDetailsService {

    private static final String POLITICA_CLAVE = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";

    private final UsuarioRepository repository;
    private final ProfesionalRepository profesionalRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, ProfesionalRepository profesionalRepository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.profesionalRepository = profesionalRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ------------------------------------------------- inicio de sesión (Spring Security)

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario inexistente"));
        boolean profesionalDeBaja = u.getProfesional() != null && !u.getProfesional().isActivo();
        return User.withUsername(u.getUsername())
                .password(u.getPassword())
                .roles(u.getRol().name())
                .disabled(!u.isActivo() || profesionalDeBaja)
                .build();
    }

    @Transactional(readOnly = true)
    public Usuario porUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario \"" + username + "\" no encontrado"));
    }

    // ------------------------------------------------- ABM de usuarios (solo ADMIN)

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repository.findAllByOrderByUsernameAsc();
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    @Transactional
    public Usuario crear(Usuario u) {
        u.setId(null);
        if (u.getPassword() == null || u.getPassword().isBlank()) {
            throw new ReglaNegocioException("La contraseña es obligatoria");
        }
        validarClave(u.getPassword());
        if (repository.existsByUsername(u.getUsername())) {
            throw new ReglaNegocioException("Ya existe el usuario \"" + u.getUsername() + "\"");
        }
        resolverProfesional(u, null);
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        u.setActivo(true);
        return repository.save(u);
    }

    @Transactional
    public Usuario actualizar(Long id, Usuario datos) {
        Usuario actual = obtener(id);
        if (repository.existsByUsernameAndIdNot(datos.getUsername(), id)) {
            throw new ReglaNegocioException("Ya existe otro usuario \"" + datos.getUsername() + "\"");
        }
        resolverProfesional(datos, id);
        if (datos.getPassword() != null && !datos.getPassword().isBlank()) {
            validarClave(datos.getPassword());
            actual.setPassword(passwordEncoder.encode(datos.getPassword()));
        }
        actual.setUsername(datos.getUsername());
        actual.setRol(datos.getRol());
        actual.setActivo(datos.isActivo());
        actual.setProfesional(datos.getProfesional());
        return repository.save(actual);
    }

    @Transactional
    public void darDeBaja(Long id, String usernameActual) {
        Usuario u = obtener(id);
        if (u.getUsername().equals(usernameActual)) {
            throw new ReglaNegocioException("No podés dar de baja tu propio usuario");
        }
        u.setActivo(false);
        repository.save(u);
    }

    // ------------------------------------------------- reglas

    private void validarClave(String clave) {
        if (!clave.matches(POLITICA_CLAVE)) {
            throw new ReglaNegocioException("La contraseña debe tener al menos 8 caracteres, con letras y números");
        }
    }

    /** El rol PROFESIONAL exige un profesional activo sin otro usuario; el resto de los roles no lleva vínculo. */
    private void resolverProfesional(Usuario u, Long idUsuario) {
        if (u.getRol() != Rol.PROFESIONAL) {
            u.setProfesional(null);
            return;
        }
        if (u.getProfesional() == null || u.getProfesional().getId() == null) {
            throw new ReglaNegocioException("Un usuario con rol PROFESIONAL debe estar vinculado a un profesional");
        }
        Long profId = u.getProfesional().getId();
        Profesional p = profesionalRepository.findById(profId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Profesional", profId));
        if (!p.isActivo()) {
            throw new ReglaNegocioException("El profesional elegido está dado de baja");
        }
        if (repository.existsByProfesionalIdAndIdNot(profId, idUsuario == null ? -1L : idUsuario)) {
            throw new ReglaNegocioException("Ese profesional ya tiene un usuario asignado");
        }
        u.setProfesional(p);
    }
}

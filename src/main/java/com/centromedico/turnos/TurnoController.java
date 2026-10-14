package com.centromedico.turnos;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.centromedico.usuarios.Rol;
import com.centromedico.usuarios.Usuario;
import com.centromedico.usuarios.UsuarioService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
public class TurnoController {

    /** Cuerpo de PUT /api/turnos/{id}/estado */
    public record CambioEstado(@NotNull(message = "Indique el nuevo estado") EstadoTurno estado) {
    }

    private final TurnoService service;
    private final UsuarioService usuarioService;

    public TurnoController(TurnoService service, UsuarioService usuarioService) {
        this.service = service;
        this.usuarioService = usuarioService;
    }

    /** GET /api/turnos?fecha=2026-10-12&estado=EN_ESPERA&pacienteId=3&profesionalId=1 */
    @GetMapping("/api/turnos")
    public List<Turno> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) EstadoTurno estado,
            @RequestParam(required = false) Long pacienteId,
            @RequestParam(required = false) Long profesionalId,
            Authentication auth) {
        Usuario u = usuarioService.porUsername(auth.getName());
        // Un profesional solo ve sus propios turnos, aunque pida otros.
        Long filtroProfesional = (u.getRol() == Rol.PROFESIONAL) ? u.getProfesional().getId() : profesionalId;
        return service.listar(fecha, estado, pacienteId, filtroProfesional);
    }

    @GetMapping("/api/turnos/{id}")
    public Turno obtener(@PathVariable Long id, Authentication auth) {
        Turno t = service.obtener(id);
        verificarAgendaPropia(auth, t.getProfesional().getId());
        return t;
    }

    @PostMapping("/api/turnos")
    @ResponseStatus(HttpStatus.CREATED)
    public Turno crear(@Valid @RequestBody Turno turno, Authentication auth) {
        return service.crear(turno, auth.getName());
    }

    @PutMapping("/api/turnos/{id}")
    public Turno actualizar(@PathVariable Long id, @Valid @RequestBody Turno turno) {
        return service.actualizar(id, turno);
    }

    /** Cambia el estado: RESERVADO -> EN_ESPERA, CANCELADO o AUSENTE. (ATENDIDO se da al registrar la atención.) */
    @PutMapping("/api/turnos/{id}/estado")
    public Turno cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstado cambio) {
        return service.cambiarEstado(id, cambio.estado());
    }

    /** DELETE cancela el turno (baja lógica): el horario queda libre pero el registro se conserva. */
    @DeleteMapping("/api/turnos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Long id) {
        service.cancelar(id);
    }

    /** GET /api/profesionales/{id}/turnos?fecha=AAAA-MM-DD  (si no se indica fecha, es hoy) */
    @GetMapping("/api/profesionales/{id}/turnos")
    public List<Turno> agenda(@PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Authentication auth) {
        verificarAgendaPropia(auth, id);
        return service.agenda(id, fecha != null ? fecha : LocalDate.now());
    }

    /** GET /api/profesionales/{id}/horarios-libres?fecha=AAAA-MM-DD */
    @GetMapping("/api/profesionales/{id}/horarios-libres")
    public List<String> horariosLibres(@PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return service.horariosLibres(id, fecha);
    }

    /** RN-10: un profesional solo puede ver su propia agenda. */
    private void verificarAgendaPropia(Authentication auth, Long profesionalId) {
        Usuario u = usuarioService.porUsername(auth.getName());
        if (u.getRol() == Rol.PROFESIONAL && !u.getProfesional().getId().equals(profesionalId)) {
            throw new AccessDeniedException("Solo puede consultar su propia agenda");
        }
    }
}

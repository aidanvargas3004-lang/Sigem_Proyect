package com.transur.sigem.sistema.controller;

import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.UsuarioRepository;
import com.transur.sigem.sistema.dto.NotificacionResponse;
import com.transur.sigem.sistema.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API de notificaciones. Cada usuario solo consulta y marca las suyas;
 * el administrador puede consultar las de cualquiera y crear avisos manuales.
 */
@RestController
@RequestMapping("/api/sistema/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notifService;
    private final UsuarioRepository usuarioRepository;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<NotificacionResponse> crearManual(
            @RequestParam Long usuarioId,
            @RequestParam String titulo,
            @RequestParam String mensaje,
            @RequestParam(defaultValue = "INFO") String tipo) {
        return ResponseEntity.ok(notifService.crearManual(usuarioId, titulo, mensaje, tipo));
    }

    @GetMapping("/no-leidas/{usuarioId}")
    public ResponseEntity<List<NotificacionResponse>> listarNoLeidas(@PathVariable Long usuarioId, Authentication auth) {
        verificarPropietario(usuarioId, auth);
        return ResponseEntity.ok(notifService.listarNoLeidas(usuarioId));
    }

    @GetMapping("/count/{usuarioId}")
    public ResponseEntity<Long> contarNoLeidas(@PathVariable Long usuarioId, Authentication auth) {
        verificarPropietario(usuarioId, auth);
        return ResponseEntity.ok(notifService.contarNoLeidas(usuarioId));
    }

    @PutMapping("/{id}/leida")
    public ResponseEntity<Void> marcarLeida(@PathVariable Long id, Authentication auth) {
        notifService.marcarLeidaDeUsuario(id, actual(auth).getId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/marcar-todas-leidas/{usuarioId}")
    public ResponseEntity<Void> marcarTodasLeidas(@PathVariable Long usuarioId, Authentication auth) {
        verificarPropietario(usuarioId, auth);
        notifService.marcarTodasComoLeidas(usuarioId);
        return ResponseEntity.ok().build();
    }

    private Usuario actual(Authentication auth) {
        return usuarioRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("Usuario no válido"));
    }

    private void verificarPropietario(Long usuarioId, Authentication auth) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (!admin && !actual(auth).getId().equals(usuarioId)) {
            throw new AccessDeniedException("Solo puede consultar sus propias notificaciones");
        }
    }
}

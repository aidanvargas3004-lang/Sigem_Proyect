package com.transur.sigem.sistema.controller;

import com.transur.sigem.sistema.dto.NotificacionResponse;
import com.transur.sigem.sistema.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sistema/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notifService;

    // ✅ NUEVO: Crear notificación manual (POST)
    @PostMapping
    public ResponseEntity<NotificacionResponse> crearManual(
            @RequestParam Long usuarioId,
            @RequestParam String titulo,
            @RequestParam String mensaje,
            @RequestParam(defaultValue = "INFO") String tipo) {

        return ResponseEntity.ok(notifService.crearManual(usuarioId, titulo, mensaje, tipo));
    }

    // ✅ EXISTENTE: Listar no leídas
    @GetMapping("/no-leidas/{usuarioId}")
    public ResponseEntity<List<NotificacionResponse>> listarNoLeidas(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(notifService.listarNoLeidas(usuarioId));
    }

    // ✅ EXISTENTE: Contar no leídas (para badge rojo)
    @GetMapping("/count/{usuarioId}")
    public ResponseEntity<Long> contarNoLeidas(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(notifService.contarNoLeidas(usuarioId));
    }

    // ✅ EXISTENTE: Marcar una como leída
    @PutMapping("/{id}/leida")
    public ResponseEntity<Void> marcarLeida(@PathVariable Long id) {
        notifService.marcarComoLeida(id);
        return ResponseEntity.ok().build();
    }

    // ✅ NUEVO: Marcar TODAS las notificaciones de un usuario como leídas
    @PutMapping("/marcar-todas-leidas/{usuarioId}")
    public ResponseEntity<Void> marcarTodasLeidas(@PathVariable Long usuarioId) {
        notifService.marcarTodasComoLeidas(usuarioId);
        return ResponseEntity.ok().build();
    }
}
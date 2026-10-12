package com.transur.sigem.operaciones.controller;

import com.transur.sigem.operaciones.dto.ServicioRequest;
import com.transur.sigem.operaciones.dto.ServicioResponse;
import com.transur.sigem.operaciones.service.ServicioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioService servicioService;

    @PostMapping
    public ResponseEntity<ServicioResponse> crear(@Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.status(201).body(servicioService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<ServicioResponse>> listar() {
        return ResponseEntity.ok(servicioService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicioResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(servicioService.obtener(id));
    }

    // ✅ CRÍTICO: Cambiar estado del servicio (Ej: POR_ASIGNAR -> EN_CURSO -> FINALIZADO)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ServicioResponse> actualizarEstado(
            @PathVariable Long id,
            @RequestParam String nuevoEstado) {
        return ResponseEntity.ok(servicioService.actualizarEstado(id, nuevoEstado));
    }

    // ✅ Útil para ver todos los servicios de un contrato específico
    @GetMapping("/contrato/{contratoId}")
    public ResponseEntity<List<ServicioResponse>> listarPorContrato(@PathVariable Long contratoId) {
        return ResponseEntity.ok(servicioService.listarPorContrato(contratoId));
    }
}
package com.transur.sigem.operaciones.controller;

import com.transur.sigem.operaciones.dto.AsignacionRequest;
import com.transur.sigem.operaciones.dto.AsignacionResponse;
import com.transur.sigem.operaciones.service.AsignacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asignaciones")
@RequiredArgsConstructor
public class AsignacionController {

    private final AsignacionService asignacionService;

    @PostMapping
    public ResponseEntity<AsignacionResponse> crear(@Valid @RequestBody AsignacionRequest request) {
        return ResponseEntity.status(201).body(asignacionService.crear(request));
    }

    // ✅ CRÍTICO: Cancelar o Reemplazar una asignación (Ej: si la máquina se malogra)
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<AsignacionResponse> cancelar(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(asignacionService.cancelar(id, motivo));
    }

    // ✅ Útil: Ver qué máquinas tiene asignadas un empleado hoy
    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<List<AsignacionResponse>> listarPorEmpleado(@PathVariable Long empleadoId) {
        return ResponseEntity.ok(asignacionService.listarPorEmpleado(empleadoId));
    }

    // ✅ Útil: Ver historial de uso de una maquinaria
    @GetMapping("/maquinaria/{maquinariaId}")
    public ResponseEntity<List<AsignacionResponse>> listarPorMaquinaria(@PathVariable Long maquinariaId) {
        return ResponseEntity.ok(asignacionService.listarPorMaquinaria(maquinariaId));
    }
}
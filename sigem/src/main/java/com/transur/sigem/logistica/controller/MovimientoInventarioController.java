package com.transur.sigem.logistica.controller;

import com.transur.sigem.logistica.dto.MovimientoRequest;
import com.transur.sigem.logistica.entity.MovimientoInventario;
import com.transur.sigem.logistica.service.MovimientoInventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logistica/movimientos")
@RequiredArgsConstructor
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoService;

    @PostMapping
    public ResponseEntity<MovimientoInventario> registrar(@Valid @RequestBody MovimientoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movimientoService.registrar(request));
    }

    @GetMapping("/repuesto/{repuestoId}")
    public ResponseEntity<List<MovimientoInventario>> historial(@PathVariable Long repuestoId) {
        return ResponseEntity.ok(movimientoService.historialPorRepuesto(repuestoId));
    }
}
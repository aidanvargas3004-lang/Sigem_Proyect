package com.transur.sigem.mantenimiento.controller;

import com.transur.sigem.mantenimiento.dto.MantenimientoRequest;
import com.transur.sigem.mantenimiento.dto.MantenimientoResponse;
import com.transur.sigem.mantenimiento.service.MantenimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mantenimientos")
@RequiredArgsConstructor
public class MantenimientoController {

    private final MantenimientoService mantenimientoService;

    @PostMapping
    public ResponseEntity<MantenimientoResponse> crear(@Valid @RequestBody MantenimientoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mantenimientoService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<MantenimientoResponse>> listar() {
        return ResponseEntity.ok(mantenimientoService.listar());
    }
}
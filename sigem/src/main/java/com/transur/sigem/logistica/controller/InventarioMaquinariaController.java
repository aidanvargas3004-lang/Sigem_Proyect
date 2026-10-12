package com.transur.sigem.logistica.controller;

import com.transur.sigem.logistica.dto.InventarioMaquinariaRequest;
import com.transur.sigem.logistica.entity.InventarioMaquinaria;
import com.transur.sigem.logistica.service.InventarioMaquinariaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logistica/inventario-maquinaria")
@RequiredArgsConstructor
public class InventarioMaquinariaController {

    private final InventarioMaquinariaService servicio;

    @PostMapping
    public ResponseEntity<InventarioMaquinaria> registrar(@Valid @RequestBody InventarioMaquinariaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicio.registrar(request));
    }

    @GetMapping
    public ResponseEntity<List<InventarioMaquinaria>> listar() {
        return ResponseEntity.ok(servicio.listar());
    }
}
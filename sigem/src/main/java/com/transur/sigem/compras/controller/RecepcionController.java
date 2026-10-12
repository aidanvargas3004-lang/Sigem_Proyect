package com.transur.sigem.compras.controller;

import com.transur.sigem.compras.dto.RecepcionRequest;
import com.transur.sigem.compras.entity.RecepcionCompra;
import com.transur.sigem.compras.service.RecepcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recepciones")
@RequiredArgsConstructor
public class RecepcionController {

    private final RecepcionService recepcionService;

    @PostMapping
    public ResponseEntity<RecepcionCompra> crear(@Valid @RequestBody RecepcionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recepcionService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<RecepcionCompra>> listar() {
        return ResponseEntity.ok(recepcionService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecepcionCompra> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(recepcionService.obtenerPorId(id));
    }

    // Obtener recepciones vinculadas a una OC específica
    @GetMapping("/orden/{ordenCompraId}")
    public ResponseEntity<List<RecepcionCompra>> listarPorOrden(@PathVariable Long ordenCompraId) {
        return ResponseEntity.ok(recepcionService.listarPorOrden(ordenCompraId));
    }
}
package com.transur.sigem.compras.controller;

import com.transur.sigem.compras.dto.OrdenCompraRequest;
import com.transur.sigem.compras.entity.OrdenCompra;
import com.transur.sigem.compras.service.OrdenCompraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes-compra")
@RequiredArgsConstructor
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;

    @PostMapping
    public ResponseEntity<OrdenCompra> crear(@Valid @RequestBody OrdenCompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenCompraService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<OrdenCompra>> listar() {
        return ResponseEntity.ok(ordenCompraService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenCompra> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ordenCompraService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<OrdenCompra> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String estado) {
        return ResponseEntity.ok(ordenCompraService.cambiarEstado(id, estado));
    }

    // Endpoint específico para cambiar estado a CANCELADA
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<OrdenCompra> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(ordenCompraService.cancelar(id));
    }
}
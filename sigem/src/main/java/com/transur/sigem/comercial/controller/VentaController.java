package com.transur.sigem.comercial.controller;

import com.transur.sigem.comercial.dto.VentaRequest;
import com.transur.sigem.comercial.entity.Venta;
import com.transur.sigem.comercial.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {
    private final VentaService ventaService;

    @PostMapping
    public ResponseEntity<Venta> crear(@Valid @RequestBody VentaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.crear(req));
    }

    @GetMapping
    public ResponseEntity<List<Venta>> listar() {
        return ResponseEntity.ok(ventaService.listar());
    }
}
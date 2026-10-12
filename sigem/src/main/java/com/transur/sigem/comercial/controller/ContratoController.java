package com.transur.sigem.comercial.controller;

import com.transur.sigem.comercial.dto.ContratoRequest;
import com.transur.sigem.comercial.dto.ContratoResponse;
import com.transur.sigem.comercial.service.ContratoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/contratos")
@RequiredArgsConstructor
public class ContratoController {

    private final ContratoService contratoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('VENTAS', 'ADMIN')")
    public ResponseEntity<ContratoResponse> crear(@Valid @RequestBody ContratoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contratoService.crear(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VENTAS', 'ADMIN')")
    public ResponseEntity<List<ContratoResponse>> listar() {
        return ResponseEntity.ok(contratoService.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VENTAS', 'ADMIN')")
    public ResponseEntity<ContratoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(contratoService.obtener(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('VENTAS', 'ADMIN')")
    public ResponseEntity<ContratoResponse> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody ContratoRequest request) {
        return ResponseEntity.ok(contratoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('VENTAS', 'ADMIN')")
    public ResponseEntity<ContratoResponse> cambiarEstado(@PathVariable Long id,
                                                           @RequestParam String estado) {
        return ResponseEntity.ok(contratoService.cambiarEstado(id, estado));
    }
}

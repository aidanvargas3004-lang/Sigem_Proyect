package com.transur.sigem.comercial.controller;

import com.transur.sigem.comercial.dto.CotizacionRequest;
import com.transur.sigem.comercial.dto.CotizacionResponse;
import com.transur.sigem.comercial.service.CotizacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/cotizaciones")
@RequiredArgsConstructor
public class CotizacionController {

    private final CotizacionService cotizacionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('VENTAS', 'ADMIN')")
    public ResponseEntity<CotizacionResponse> crear(@Valid @RequestBody CotizacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cotizacionService.crear(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VENTAS', 'ADMIN')")
    public ResponseEntity<List<CotizacionResponse>> listar() {
        return ResponseEntity.ok(cotizacionService.listar());
    }
}
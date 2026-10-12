package com.transur.sigem.comercial.controller;

import com.transur.sigem.comercial.dto.SolicitudRequest;
import com.transur.sigem.comercial.entity.Solicitud;
import com.transur.sigem.comercial.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@RequiredArgsConstructor
public class SolicitudController {
    private final SolicitudService solicitudService;

    @PostMapping
    @PreAuthorize("hasAnyRole('VENTAS', 'ADMIN')")
    public ResponseEntity<Solicitud> crear(@Valid @RequestBody SolicitudRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(solicitudService.crear(req));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('VENTAS', 'ADMIN')")
    public ResponseEntity<List<Solicitud>> listar() {
        return ResponseEntity.ok(solicitudService.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('VENTAS', 'ADMIN')")
    public ResponseEntity<Solicitud> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(solicitudService.obtenerPorId(id));
    }
}
package com.transur.sigem.activos.controller;

import com.transur.sigem.activos.dto.AdquisicionRequest;
import com.transur.sigem.activos.dto.AdquisicionResponse;
import com.transur.sigem.activos.service.AdquisicionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activos/adquisiciones")
@RequiredArgsConstructor
public class AdquisicionController {

    private final AdquisicionService adquisicionService;

    @PostMapping
    public ResponseEntity<AdquisicionResponse> crear(@Valid @RequestBody AdquisicionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adquisicionService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<AdquisicionResponse>> listar() {
        return ResponseEntity.ok(adquisicionService.listar());
    }

    // En AdquisicionController.java

    @PutMapping("/{id}")
    public ResponseEntity<AdquisicionResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody AdquisicionRequest request) {
        return ResponseEntity.ok(adquisicionService.actualizar(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdquisicionResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(adquisicionService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        adquisicionService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}
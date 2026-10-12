package com.transur.sigem.marketing.controller;

import com.transur.sigem.marketing.dto.OportunidadRequest;
import com.transur.sigem.marketing.dto.OportunidadResponse;
import com.transur.sigem.marketing.service.OportunidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketing/oportunidades")
@RequiredArgsConstructor
public class OportunidadController {

    private final OportunidadService oportunidadService;

    @PostMapping
    public ResponseEntity<OportunidadResponse> crear(@Valid @RequestBody OportunidadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(oportunidadService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<OportunidadResponse>> listar() {
        return ResponseEntity.ok(oportunidadService.listar());
    }

    @PutMapping("/{id}")
    public ResponseEntity<OportunidadResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody OportunidadRequest request) {
        return ResponseEntity.ok(oportunidadService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        oportunidadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
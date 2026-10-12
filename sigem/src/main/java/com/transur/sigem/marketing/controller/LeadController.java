package com.transur.sigem.marketing.controller;

import com.transur.sigem.marketing.dto.LeadRequest;
import com.transur.sigem.marketing.entity.Lead;
import com.transur.sigem.marketing.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/marketing/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    public ResponseEntity<Lead> crear(@Valid @RequestBody LeadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leadService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<Lead>> listar() {
        return ResponseEntity.ok(leadService.listar());
    }

    @GetMapping("/campania/{campaniaId}")
    public ResponseEntity<List<Lead>> listarPorCampania(@PathVariable Long campaniaId) {
        return ResponseEntity.ok(leadService.listarPorCampania(campaniaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lead> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody LeadRequest request) {
        return ResponseEntity.ok(leadService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        leadService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
package com.transur.sigem.maquinaria.controller;

import com.transur.sigem.maquinaria.dto.TarifaRequest;
import com.transur.sigem.maquinaria.dto.TarifaResponse;
import com.transur.sigem.maquinaria.service.TarifaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/maquinaria/tarifas")
@RequiredArgsConstructor
public class TarifaController {

    private final TarifaService service;
    private final TarifaService tarifaService;

    @PostMapping
    public ResponseEntity<TarifaResponse> crear(@Valid @RequestBody TarifaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @GetMapping("/maquinaria/{id}")
    public ResponseEntity<List<TarifaResponse>> listarPorMaquinaria(@PathVariable Long id) {
        return ResponseEntity.ok(tarifaService.listarPorMaquinaria(id));
    }


}
package com.transur.sigem.comercial.controller;

import com.transur.sigem.comercial.dto.AlquilerRequest;
import com.transur.sigem.comercial.entity.Alquiler;
import com.transur.sigem.comercial.service.AlquilerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alquileres")
@RequiredArgsConstructor
public class AlquilerController {
    private final AlquilerService alquilerService;

    @PostMapping
    public ResponseEntity<Alquiler> crear(@Valid @RequestBody AlquilerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(alquilerService.crear(req));
    }

    @GetMapping
    public ResponseEntity<List<Alquiler>> listar() {
        return ResponseEntity.ok(alquilerService.listar());
    }
}
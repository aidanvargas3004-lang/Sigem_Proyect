package com.transur.sigem.logistica.controller;

import com.transur.sigem.logistica.dto.AlmacenRequest;
import com.transur.sigem.logistica.dto.UbicacionRequest;
import com.transur.sigem.logistica.entity.Almacen;
import com.transur.sigem.logistica.entity.UbicacionAlmacen;
import com.transur.sigem.logistica.service.AlmacenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logistica/almacenes")
@RequiredArgsConstructor
public class AlmacenController {

    private final AlmacenService almacenService;

    @PostMapping
    public ResponseEntity<Almacen> crear(@Valid @RequestBody AlmacenRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(almacenService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<Almacen>> listar() {
        return ResponseEntity.ok(almacenService.listar());
    }

    @PostMapping("/ubicaciones")
    public ResponseEntity<UbicacionAlmacen> crearUbicacion(@Valid @RequestBody UbicacionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(almacenService.crearUbicacion(request));
    }

    @GetMapping("/{almacenId}/ubicaciones")
    public ResponseEntity<List<UbicacionAlmacen>> listarUbicaciones(@PathVariable Long almacenId) {
        return ResponseEntity.ok(almacenService.listarUbicaciones(almacenId));
    }
}
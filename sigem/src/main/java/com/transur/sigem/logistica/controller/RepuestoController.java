package com.transur.sigem.logistica.controller;

import com.transur.sigem.common.service.CloudinaryService;
import com.transur.sigem.logistica.dto.RepuestoRequest;
import com.transur.sigem.logistica.entity.Repuesto;
import com.transur.sigem.logistica.repository.RepuestoRepository;
import com.transur.sigem.logistica.service.RepuestoService;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/logistica/repuestos")
@RequiredArgsConstructor
public class RepuestoController {

    private final RepuestoService repuestoService;
    private final RepuestoRepository repuestoRepository;
    private final CloudinaryService cloudinaryService;

    @PostMapping
    public ResponseEntity<Repuesto> crear(@Valid @RequestBody RepuestoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repuestoService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<Repuesto>> listar() {
        return ResponseEntity.ok(repuestoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Repuesto> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(repuestoService.obtenerPorId(id));
    }

    @PostMapping("/{id}/imagen")
    public ResponseEntity<Repuesto> subirImagen(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Repuesto r = repuestoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repuesto no encontrado"));
        r.setImagenUrl(cloudinaryService.uploadImage(file));
        return ResponseEntity.ok(r);
    }
}
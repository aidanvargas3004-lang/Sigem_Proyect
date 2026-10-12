package com.transur.sigem.operaciones.controller;

import com.transur.sigem.common.service.CloudinaryService;
import com.transur.sigem.operaciones.entity.Incidencia;
import com.transur.sigem.operaciones.repository.IncidenciaRepository;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/incidencias")
@RequiredArgsConstructor
public class IncidenciaController {

    private final IncidenciaRepository incidenciaRepository;
    private final CloudinaryService cloudinaryService;

    @PostMapping
    public ResponseEntity<Incidencia> reportar(@RequestBody Incidencia incidencia) {
        incidencia.setFecha(LocalDateTime.now());
        incidencia.setEstado("ABIERTA");
        return ResponseEntity.status(201).body(incidenciaRepository.save(incidencia));
    }

    @GetMapping("/servicio/{servicioId}")
    public ResponseEntity<List<Incidencia>> listarPorServicio(@PathVariable Long servicioId) {
        return ResponseEntity.ok(incidenciaRepository.findByServicioId(servicioId));
    }

    @PostMapping("/{id}/evidencia")
    public ResponseEntity<Incidencia> subirEvidencia(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Incidencia inc = incidenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
        inc.setFotoEvidenciaUrl(cloudinaryService.uploadImage(file));
        return ResponseEntity.ok(inc);
    }
}
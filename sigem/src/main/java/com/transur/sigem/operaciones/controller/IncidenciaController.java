package com.transur.sigem.operaciones.controller;

import com.transur.sigem.common.exception.RecursoNoEncontradoException;
import com.transur.sigem.common.service.CloudinaryService;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.operaciones.dto.IncidenciaRequest;
import com.transur.sigem.operaciones.dto.IncidenciaResponse;
import com.transur.sigem.operaciones.entity.Incidencia;
import com.transur.sigem.operaciones.repository.IncidenciaRepository;
import com.transur.sigem.operaciones.repository.ServicioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/incidencias")
@RequiredArgsConstructor
public class IncidenciaController {

    private final IncidenciaRepository incidenciaRepository;
    private final ServicioRepository servicioRepository;
    private final MaquinariaRepository maquinariaRepository;
    private final CloudinaryService cloudinaryService;

    @PostMapping
    @Transactional
    public ResponseEntity<IncidenciaResponse> reportar(@Valid @RequestBody IncidenciaRequest request) {
        Incidencia incidencia = new Incidencia();
        incidencia.setServicio(servicioRepository.findById(request.servicioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", request.servicioId())));
        if (request.maquinariaId() != null) {
            incidencia.setMaquinaria(maquinariaRepository.findById(request.maquinariaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Maquinaria", request.maquinariaId())));
        }
        incidencia.setTipo(request.tipo());
        incidencia.setDescripcion(request.descripcion());
        incidencia.setFecha(LocalDateTime.now());
        incidencia.setEstado("ABIERTA");
        return ResponseEntity.status(201).body(IncidenciaResponse.de(incidenciaRepository.save(incidencia)));
    }

    @GetMapping("/servicio/{servicioId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<IncidenciaResponse>> listarPorServicio(@PathVariable Long servicioId) {
        return ResponseEntity.ok(incidenciaRepository.findByServicioId(servicioId).stream().map(IncidenciaResponse::de).toList());
    }

    @PostMapping("/{id}/evidencia")
    @Transactional
    public ResponseEntity<IncidenciaResponse> subirEvidencia(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Incidencia inc = incidenciaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Incidencia", id));
        inc.setFotoEvidenciaUrl(cloudinaryService.uploadImage(file));
        return ResponseEntity.ok(IncidenciaResponse.de(inc));
    }
}

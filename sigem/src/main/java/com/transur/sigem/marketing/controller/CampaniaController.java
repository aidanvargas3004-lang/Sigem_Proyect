package com.transur.sigem.marketing.controller;

import com.transur.sigem.common.service.CloudinaryService;
import com.transur.sigem.marketing.dto.CampaniaRequest;
import com.transur.sigem.marketing.entity.Campania;
import com.transur.sigem.marketing.repository.CampaniaRepository;
import com.transur.sigem.marketing.service.CampaniaService;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/marketing/campanias")
@RequiredArgsConstructor
public class CampaniaController {

    private final CampaniaService campaniaService;
    private final CampaniaRepository campaniaRepository;
    private final CloudinaryService cloudinaryService;

    @PostMapping
    public ResponseEntity<Campania> crear(@Valid @RequestBody CampaniaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaniaService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<Campania>> listar() {
        return ResponseEntity.ok(campaniaService.listar());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Campania> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CampaniaRequest request) {
        return ResponseEntity.ok(campaniaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        campaniaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/banner")
    public ResponseEntity<Campania> subirBanner(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Campania c = campaniaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));
        c.setBannerUrl(cloudinaryService.uploadImage(file));
        return ResponseEntity.ok(c);
    }
}
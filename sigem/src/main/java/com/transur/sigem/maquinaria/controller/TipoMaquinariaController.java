package com.transur.sigem.maquinaria.controller;

import com.transur.sigem.maquinaria.dto.TipoMaquinariaRequest;
import com.transur.sigem.maquinaria.entity.TipoMaquinaria;
import com.transur.sigem.maquinaria.service.TipoMaquinariaService;
import com.transur.sigem.common.service.CloudinaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/maquinaria/tipos")
@RequiredArgsConstructor
public class TipoMaquinariaController {

    private final TipoMaquinariaService service;
    private final CloudinaryService cloudinaryService;

    @PostMapping
    public ResponseEntity<TipoMaquinaria> crear(@Valid @RequestBody TipoMaquinariaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PostMapping("/{id}/imagen")
    public ResponseEntity<TipoMaquinaria> subirImagen(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        TipoMaquinaria tipo = service.obtener(id);
        tipo.setImageUrl(cloudinaryService.uploadImage(file));
        return ResponseEntity.ok(service.guardar(tipo));
    }

    @GetMapping
    public ResponseEntity<List<TipoMaquinaria>> listar() {
        return ResponseEntity.ok(service.listar());
    }
}
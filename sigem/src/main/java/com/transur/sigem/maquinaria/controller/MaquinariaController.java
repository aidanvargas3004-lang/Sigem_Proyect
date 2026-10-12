package com.transur.sigem.maquinaria.controller;

import com.transur.sigem.common.service.CloudinaryService;
import com.transur.sigem.maquinaria.dto.MaquinariaRequest;
import com.transur.sigem.maquinaria.dto.MaquinariaResponse;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.entity.Tarifa;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.maquinaria.repository.TarifaRepository;
import com.transur.sigem.maquinaria.service.MaquinariaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/maquinaria")
@RequiredArgsConstructor
public class MaquinariaController {

    private final MaquinariaService maquinariaService;
    private final MaquinariaRepository maquinariaRepository;
    private final CloudinaryService cloudinaryService;
    private final TarifaRepository tarifaRepository;

    @PostMapping
    public ResponseEntity<MaquinariaResponse> crear(@Valid @RequestBody MaquinariaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(maquinariaService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<MaquinariaResponse>> listar() {
        return ResponseEntity.ok(maquinariaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaquinariaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(maquinariaService.obtenerPorId(id));
    }

    // ✅ AGREGA ESTO PARA UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<MaquinariaResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MaquinariaRequest request) {
        return ResponseEntity.ok(maquinariaService.actualizar(id, request));
    }

    // ✅ AGREGA ESTO PARA DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        maquinariaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ NUEVO ENDPOINT PARA SUBIR FOTO
    @PostMapping("/{id}/imagen")
    public ResponseEntity<MaquinariaResponse> subirImagen(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        // 1. Validar que sea imagen
        if (file.isEmpty() || !file.getContentType().startsWith("image/")) {
            throw new RuntimeException("Solo se permiten archivos de imagen válidos");
        }

        // 2. Verificar que la maquinaria exista
        Maquinaria maq = maquinariaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maquinaria no encontrada con ID: " + id));

        // 3. Subir a Cloudinary
        String imageUrl = cloudinaryService.uploadImage(file);

        // 4. Actualizar entidad y guardar
        maq.setImageUrl(imageUrl);
        maquinariaRepository.save(maq);

        // 5. Retornar respuesta actualizada
        return ResponseEntity.ok(maquinariaService.obtenerPorId(id));
    }

    

}
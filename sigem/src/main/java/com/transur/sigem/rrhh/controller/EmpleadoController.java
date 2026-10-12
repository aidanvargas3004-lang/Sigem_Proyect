package com.transur.sigem.rrhh.controller;

import com.transur.sigem.common.service.CloudinaryService;
import com.transur.sigem.rrhh.dto.EmpleadoRequest;
import com.transur.sigem.rrhh.dto.EmpleadoResponse;
import com.transur.sigem.rrhh.entity.Empleado;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import com.transur.sigem.rrhh.service.EmpleadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoService empleadoService;
    private final EmpleadoRepository empleadoRepository;
    private final CloudinaryService cloudinaryService;

    @PostMapping
    public ResponseEntity<EmpleadoResponse> crear(@Valid @RequestBody EmpleadoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(empleadoService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmpleadoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EmpleadoRequest request) {
        return ResponseEntity.ok(empleadoService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Void> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean estado) {
        empleadoService.cambiarEstado(id, estado);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<EmpleadoResponse>> listar() {
        return ResponseEntity.ok(empleadoService.listarTodos());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<EmpleadoResponse>> listarActivos() {
        return ResponseEntity.ok(empleadoService.listarActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpleadoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(empleadoService.obtenerPorId(id));
    }

    @PostMapping("/{id}/foto-perfil")
    public ResponseEntity<EmpleadoResponse> subirFotoPerfil(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Empleado emp = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        emp.setFotoPerfilUrl(cloudinaryService.uploadImage(file));
        return ResponseEntity.ok(empleadoService.obtenerPorId(id));
    }

    @PostMapping("/{id}/licencia")
    public ResponseEntity<EmpleadoResponse> subirLicencia(
            @PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Empleado emp = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        emp.setLicenciaUrl(cloudinaryService.uploadImage(file));
        return ResponseEntity.ok(empleadoService.obtenerPorId(id));
    }
}
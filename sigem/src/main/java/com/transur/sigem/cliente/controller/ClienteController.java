package com.transur.sigem.cliente.controller;

import com.transur.sigem.cliente.dto.ClienteRequest;
import com.transur.sigem.cliente.dto.ClienteResponse;
import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.cliente.repository.ClienteRepository;
import com.transur.sigem.cliente.service.ClienteService;
import com.transur.sigem.common.service.CloudinaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteRepository clienteRepository;
    private final CloudinaryService cloudinaryService;

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clienteService.crear(request));
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/logo")
    public ResponseEntity<Cliente> subirLogo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        // 1. Validar que sea imagen y no esté vacío
        if (file.isEmpty() || !file.getContentType().startsWith("image/")) {
            throw new RuntimeException("Solo se permiten archivos de imagen válidos");
        }

        // 2. Validar tamaño máximo (ej: 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("La imagen no puede superar los 5MB");
        }

        // 3. Verificar que el cliente exista
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));

        // 4. Subir a Cloudinary (reutiliza el mismo servicio de Maquinaria)
        String logoUrl = cloudinaryService.uploadImage(file);

        // 5. Actualizar entidad y guardar
        cliente.setLogoUrl(logoUrl);
        clienteRepository.save(cliente);

        // 6. Retornar cliente actualizado
        return ResponseEntity.ok(cliente);
    }
}
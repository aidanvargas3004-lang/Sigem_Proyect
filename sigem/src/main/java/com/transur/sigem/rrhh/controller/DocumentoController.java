package com.transur.sigem.rrhh.controller;

import com.transur.sigem.rrhh.dto.DocumentoRequest;
import com.transur.sigem.rrhh.dto.DocumentoResponse;
import com.transur.sigem.rrhh.service.DocumentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
public class DocumentoController {

    private final DocumentoService documentoService;

    @PostMapping
    public ResponseEntity<DocumentoResponse> crear(@Valid @RequestBody DocumentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentoService.crear(request));
    }

    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<List<DocumentoResponse>> listarPorEmpleado(@PathVariable Long empleadoId) {
        return ResponseEntity.ok(documentoService.listarPorEmpleado(empleadoId));
    }

    @GetMapping("/proximos-vencimiento")
    public ResponseEntity<List<DocumentoResponse>> listarProximosAVencer() {
        return ResponseEntity.ok(documentoService.listarProximosAVencer());
    }

    @GetMapping("/vencidos")
    public ResponseEntity<List<DocumentoResponse>> listarVencidos() {
        return ResponseEntity.ok(documentoService.listarVencidos());
    }
}
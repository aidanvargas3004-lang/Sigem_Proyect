package com.transur.sigem.sistema.controller;

import com.transur.sigem.sistema.dto.ResumenAuditoriaDTO;
import com.transur.sigem.sistema.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sistema/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    // ✅ NUEVO: Endpoint para registrar auditoría manualmente (POST)
    @PostMapping
    public ResponseEntity<Void> registrar(
            @RequestParam Long usuarioId,
            @RequestParam String modulo,
            @RequestParam String accion,
            @RequestParam String entidad,
            @RequestParam Long entidadId,
            @RequestParam(required = false) String datosAnteriores,
            @RequestParam(required = false) String datosNuevos) {

        auditoriaService.registrar(usuarioId, modulo, accion, entidad, entidadId, datosAnteriores, datosNuevos);
        return ResponseEntity.ok().build();
    }

    // ✅ EXISTENTE: Listar por módulo
    @GetMapping("/modulo/{modulo}")
    public ResponseEntity<List<ResumenAuditoriaDTO>> listarPorModulo(@PathVariable String modulo) {
        return ResponseEntity.ok(auditoriaService.listarPorModulo(modulo));
    }

    // ✅ EXTRA ÚTIL: Listar últimas 10 acciones globales (para dashboard de gerencia)
    @GetMapping("/ultimas")
    public ResponseEntity<List<ResumenAuditoriaDTO>> listarUltimas() {
        return ResponseEntity.ok(auditoriaService.listarUltimas(10));
    }
}
package com.transur.sigem.logistica.controller;

import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.logistica.dto.ResultadoImportacion;
import com.transur.sigem.logistica.service.RepuestoImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/** Importación de repuestos desde Excel y descarga de la plantilla (Logística / Almacén). */
@Controller
@RequestMapping("/logistica/repuestos")
@RequiredArgsConstructor
public class RepuestoImportController {

    private final RepuestoImportService importService;

    @PostMapping("/importar")
    public String importar(@RequestParam("archivo") MultipartFile archivo, RedirectAttributes ra) {
        try {
            ResultadoImportacion r = importService.importar(archivo.getOriginalFilename(), archivo.getInputStream());
            ra.addFlashAttribute("mensaje", "Importación terminada: " + r.creados() + " repuesto(s) nuevo(s) y "
                    + r.actualizados() + " actualizado(s)" + (r.errores().isEmpty() ? "." : "; " + r.errores().size() + " fila(s) con error."));
            ra.addFlashAttribute("erroresImportacion", r.errores());
        } catch (NegocioException | IOException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/logistica/repuestos";
    }

    @GetMapping("/plantilla")
    public ResponseEntity<byte[]> plantilla() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename("plantilla_repuestos.xlsx").build().toString())
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(importService.plantilla());
    }
}

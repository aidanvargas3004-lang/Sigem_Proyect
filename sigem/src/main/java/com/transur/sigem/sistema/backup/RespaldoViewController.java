package com.transur.sigem.sistema.backup;

import com.transur.sigem.common.exception.NegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;

/** Respaldos y configuración de la base de datos. Solo ADMIN (regla /seguridad/** en SecurityConfig). */
@Controller
@RequestMapping("/seguridad/respaldos")
@RequiredArgsConstructor
public class RespaldoViewController {

    private final RespaldoService respaldoService;

    @GetMapping
    public String ver(Model model) {
        model.addAttribute("respaldos", respaldoService.listar());
        model.addAttribute("config", respaldoService.configuracion());
        return "seguridad/respaldos";
    }

    @PostMapping("/generar")
    public String generar(RedirectAttributes ra) {
        try {
            RespaldoInfo r = respaldoService.generar("MANUAL");
            ra.addFlashAttribute("success", "Respaldo generado: " + r.nombre() + " (" + r.getTamano() + ")");
        } catch (NegocioException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/seguridad/respaldos";
    }

    @GetMapping("/{nombre}/descargar")
    public ResponseEntity<Resource> descargar(@PathVariable String nombre) {
        Path p = respaldoService.archivo(nombre);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .contentType(MediaType.parseMediaType("application/sql"))
                .body(new FileSystemResource(p));
    }
}

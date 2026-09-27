package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.audit.AuditoriaService;
import com.transmaqsur.sigem.model.enums.AccionAuditoria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/** Consulta de la bitácora de auditoría y trazabilidad. */
@Controller
@RequestMapping("/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    public String buscar(@RequestParam(required = false) String usuario, @RequestParam(required = false) String entidad,
                         @RequestParam(required = false) AccionAuditoria accion, @RequestParam(required = false) LocalDate desde,
                         @RequestParam(required = false) LocalDate hasta, @RequestParam(required = false) Long entidadId,
                         @RequestParam(defaultValue = "0") int pagina, Model model) {
        if (entidad != null && !entidad.isBlank() && entidadId != null) {
            model.addAttribute("historialRegistro", auditoriaService.historial(entidad, entidadId));
            model.addAttribute("entidadId", entidadId);
        }
        model.addAttribute("pagina", auditoriaService.buscar(usuario, entidad, accion, desde, hasta, PageRequest.of(pagina, 30)));
        model.addAttribute("entidades", auditoriaService.entidades());
        model.addAttribute("acciones", AccionAuditoria.values());
        model.addAttribute("usuario", usuario);
        model.addAttribute("entidad", entidad);
        model.addAttribute("accion", accion);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        return "auditoria/lista";
    }
}

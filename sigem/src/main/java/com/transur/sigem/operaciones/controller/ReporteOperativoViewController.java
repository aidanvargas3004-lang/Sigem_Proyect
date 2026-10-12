package com.transur.sigem.operaciones.controller;

import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.common.reporte.ExportadorReporteFactory;
import com.transur.sigem.operaciones.service.ReporteOperativoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/** Reportes operativos con filtros y exportación (RF016, RF017). Acceso: OPERACIONES, GERENCIA, ADMIN. */
@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteOperativoViewController {

    private final ReporteOperativoService reporteService;
    private final ExportadorReporteFactory exportadores;

    @GetMapping
    public String ver(@RequestParam(defaultValue = "servicios") String tipo,
                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                      Model model) {
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        LocalDate inicio = desde != null ? desde : fin.minusDays(30);
        model.addAttribute("tipos", ReporteOperativoService.TIPOS);
        model.addAttribute("tipo", ReporteOperativoService.TIPOS.containsKey(tipo) ? tipo : "servicios");
        model.addAttribute("desde", inicio);
        model.addAttribute("hasta", fin);
        model.addAttribute("active", "reportes");
        try {
            model.addAttribute("kpi", reporteService.indicadores(inicio, fin));
            model.addAttribute("tabla", reporteService.generar((String) model.getAttribute("tipo"), inicio, fin));
        } catch (NegocioException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "operaciones/reportes";
    }

    @GetMapping("/exportar/{formato}")
    public ResponseEntity<byte[]> exportar(@PathVariable String formato,
                                           @RequestParam String tipo,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return exportadores.descargar(formato, "reporte_" + tipo, reporteService.generar(tipo, desde, hasta));
    }
}

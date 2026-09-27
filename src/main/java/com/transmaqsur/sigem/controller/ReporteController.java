package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.reporte.ExportadorExcel;
import com.transmaqsur.sigem.reporte.ExportadorPdf;
import com.transmaqsur.sigem.reporte.Reporte;
import com.transmaqsur.sigem.reporte.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/** Reportes de gestión con exportación a Excel y PDF. */
@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;
    private final ExportadorExcel exportadorExcel;
    private final ExportadorPdf exportadorPdf;

    @GetMapping
    public String catalogo(Model model) {
        model.addAttribute("catalogo", ReporteService.CATALOGO);
        return "reportes/catalogo";
    }

    @GetMapping("/{clave}")
    public String ver(@PathVariable String clave, @RequestParam(required = false) LocalDate desde,
                      @RequestParam(required = false) LocalDate hasta, Model model) {
        LocalDate d = desde != null ? desde : LocalDate.now().minusMonths(6).withDayOfMonth(1);
        LocalDate h = hasta != null ? hasta : LocalDate.now();
        model.addAttribute("reporte", reporteService.generar(clave, d, h));
        model.addAttribute("desde", d);
        model.addAttribute("hasta", h);
        return "reportes/ver";
    }

    @GetMapping("/{clave}/excel")
    public ResponseEntity<byte[]> excel(@PathVariable String clave, @RequestParam LocalDate desde, @RequestParam LocalDate hasta) {
        Reporte r = reporteService.generar(clave, desde, hasta);
        return archivo(exportadorExcel.exportar(r), clave + ".xlsx",
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @GetMapping("/{clave}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable String clave, @RequestParam LocalDate desde, @RequestParam LocalDate hasta) {
        Reporte r = reporteService.generar(clave, desde, hasta);
        return archivo(exportadorPdf.exportar(r), clave + ".pdf", MediaType.APPLICATION_PDF);
    }

    private static ResponseEntity<byte[]> archivo(byte[] contenido, String nombre, MediaType tipo) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("SIGEM-" + LocalDate.now() + "-" + nombre).build().toString())
                .contentType(tipo)
                .body(contenido);
    }
}

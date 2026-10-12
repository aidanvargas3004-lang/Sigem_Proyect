package com.transur.sigem.marketing.controller;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.transur.sigem.marketing.dto.ReporteMarketingDTO;
import com.transur.sigem.marketing.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/marketing/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping
    public String verReportes(Model model) {
        int anioActual = LocalDate.now().getYear();
        // El HTML ahora recibirá los 5 campos del DTO actualizado
        model.addAttribute("reporte", reporteService.obtenerReporteMensual(anioActual));
        model.addAttribute("anio", anioActual);
        return "marketing/reportes";
    }

    // ✅ EXPORTAR A EXCEL ACTUALIZADO (5 Columnas)
    @GetMapping("/exportar/excel")
    public void exportarExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=reporte_marketing_" + LocalDate.now().getYear() + ".xlsx");

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Rendimiento Marketing");

        // Encabezados actualizados
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Mes");
        header.createCell(1).setCellValue("Leads Captados");
        header.createCell(2).setCellValue("Oportunidades Creadas");
        header.createCell(3).setCellValue("Clientes Nuevos");
        header.createCell(4).setCellValue("Valor Pipeline (S/)");

        // Llenado de datos con los nuevos campos
        List<ReporteMarketingDTO> data = reporteService.obtenerReporteMensual(LocalDate.now().getYear());
        int rowNum = 1;
        for (ReporteMarketingDTO row : data) {
            Row excelRow = sheet.createRow(rowNum++);
            excelRow.createCell(0).setCellValue(row.getMes());
            excelRow.createCell(1).setCellValue(row.getLeads());
            excelRow.createCell(2).setCellValue(row.getOportunidadesCreadas());
            excelRow.createCell(3).setCellValue(row.getClientesNuevos());
            excelRow.createCell(4).setCellValue(row.getMontoPipeline());
        }

        // Autoajustar columnas
        for (int i = 0; i < 5; i++) sheet.autoSizeColumn(i);

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    // ✅ EXPORTAR A PDF ACTUALIZADO (5 Columnas)
    @GetMapping("/exportar/pdf")
    public void exportarPdf(HttpServletResponse response) throws Exception {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporte_marketing_" + LocalDate.now().getYear() + ".pdf");

        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        document.add(new Paragraph("REPORTE INTEGRAL DE MARKETING - SIGEM"));
        document.add(new Paragraph("Periodo: " + LocalDate.now().getYear()));
        document.add(new Paragraph(" "));

        // Tabla de 5 columnas
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.addCell("Mes");
        table.addCell("Leads");
        table.addCell("Oportunidades");
        table.addCell("Clientes Nuevos");
        table.addCell("Pipeline (S/)");

        List<ReporteMarketingDTO> data = reporteService.obtenerReporteMensual(LocalDate.now().getYear());
        for (ReporteMarketingDTO row : data) {
            table.addCell(Objects.requireNonNull(row.getMes()));
            table.addCell(String.valueOf(row.getLeads()));
            table.addCell(String.valueOf(row.getOportunidadesCreadas()));
            table.addCell(String.valueOf(row.getClientesNuevos()));
            table.addCell(String.format("%.2f", row.getMontoPipeline()));
        }

        document.add(table);
        document.close();
    }
}
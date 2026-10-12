package com.transur.sigem.comercial.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.transur.sigem.comercial.dto.ContratoResponse;
import com.transur.sigem.comercial.dto.ReporteVentasDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.math.RoundingMode;

@Service
public class ReporteExportService {
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] excel(ReporteVentasDTO r) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet resumen = workbook.createSheet("Resumen");
            int row = 0;
            row = fila(resumen, row, "REPORTE COMERCIAL SIGEM", "");
            row = fila(resumen, row, "Periodo", fecha(r) + " al " + DF.format(r.getHasta()));
            row++;
            row = fila(resumen, row, "Indicador", "Valor");
            row = fila(resumen, row, "Solicitudes", r.getTotalSolicitudes());
            row = fila(resumen, row, "Solicitudes pendientes", r.getSolicitudesPendientes());
            row = fila(resumen, row, "Cotizaciones", r.getTotalCotizaciones());
            row = fila(resumen, row, "Cotizaciones aprobadas", r.getCotizacionesAprobadas());
            row = fila(resumen, row, "Contratos", r.getTotalContratos());
            row = fila(resumen, row, "Contratos vigentes", r.getContratosVigentes());
            row = fila(resumen, row, "Contratos finalizados", r.getContratosFinalizados());
            row = fila(resumen, row, "Monto contratado", dinero(r.getMontoContratado()));
            row = fila(resumen, row, "Monto vigente", dinero(r.getMontoVigente()));
            row = fila(resumen, row, "Monto finalizado", dinero(r.getMontoFinalizado()));
            row++;
            fila(resumen, row, "Tipo", "Cantidad / Monto");
            row++;
            fila(resumen, row, "Venta", r.getContratosVenta() + " / " + dinero(r.getMontoVenta()));
            row = fila(resumen, row, "Alquiler", r.getContratosAlquiler() + " / " + dinero(r.getMontoAlquiler()));
            fila(resumen, row, "Servicio", r.getContratosServicio() + " / " + dinero(r.getMontoServicio()));
            resumen.autoSizeColumn(0); resumen.autoSizeColumn(1);

            Sheet contratos = workbook.createSheet("Contratos");
            String[] headers = {"Nro Contrato", "Cliente", "Tipo", "Inicio", "Fin", "Monto", "Estado"};
            Row h = contratos.createRow(0);
            for (int i = 0; i < headers.length; i++) h.createCell(i).setCellValue(headers[i]);
            int i = 1;
            for (ContratoResponse c : r.getContratos()) {
                Row rr = contratos.createRow(i++);
                rr.createCell(0).setCellValue(c.getNumero());
                rr.createCell(1).setCellValue(c.getClienteNombre());
                rr.createCell(2).setCellValue(c.getTipoContrato());
                rr.createCell(3).setCellValue(c.getFechaInicio() == null ? "" : DF.format(c.getFechaInicio()));
                rr.createCell(4).setCellValue(c.getFechaFin() == null ? "-" : DF.format(c.getFechaFin()));
                rr.createCell(5).setCellValue(c.getMontoTotal() == null ? 0 : c.getMontoTotal().doubleValue());
                rr.createCell(6).setCellValue(c.getEstado());
            }
            for (int c = 0; c < headers.length; c++) contratos.autoSizeColumn(c);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el Excel", e);
        }
    }

    public byte[] pdf(ReporteVentasDTO r) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 28, 28, 28, 28);
            PdfWriter.getInstance(document, out);
            document.open();

            Font title = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font normal = new Font(Font.HELVETICA, 9, Font.NORMAL);
            Font bold = new Font(Font.HELVETICA, 9, Font.BOLD);
            document.add(new Paragraph("SIGEM - REPORTE COMERCIAL", title));
            document.add(new Paragraph("Periodo: " + DF.format(r.getDesde()) + " al " + DF.format(r.getHasta()), normal));
            document.add(new Paragraph(" "));

            PdfPTable summary = new PdfPTable(4);
            summary.setWidthPercentage(100);
            addHeader(summary, "Indicador", "Valor", "Indicador", "Valor");
            addPair(summary, "Solicitudes", String.valueOf(r.getTotalSolicitudes()), "Cotizaciones", String.valueOf(r.getTotalCotizaciones()));
            addPair(summary, "Cotizaciones aprobadas", String.valueOf(r.getCotizacionesAprobadas()), "Contratos", String.valueOf(r.getTotalContratos()));
            addPair(summary, "Contratos vigentes", String.valueOf(r.getContratosVigentes()), "Contratos finalizados", String.valueOf(r.getContratosFinalizados()));
            addPair(summary, "Monto contratado", dinero(r.getMontoContratado()), "Monto vigente", dinero(r.getMontoVigente()));
            document.add(summary);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(new float[]{1.4f, 3f, 1.1f, 1.2f, 1.2f, 1.5f, 1.2f});
            table.setWidthPercentage(100);
            addHeader(table, "Contrato", "Cliente", "Tipo", "Inicio", "Fin", "Monto", "Estado");
            for (ContratoResponse c : r.getContratos()) {
                addCell(table, c.getNumero(), normal);
                addCell(table, c.getClienteNombre(), normal);
                addCell(table, c.getTipoContrato(), normal);
                addCell(table, c.getFechaInicio() == null ? "-" : DF.format(c.getFechaInicio()), normal);
                addCell(table, c.getFechaFin() == null ? "-" : DF.format(c.getFechaFin()), normal);
                addCell(table, dinero(c.getMontoTotal()), normal);
                addCell(table, c.getEstado(), normal);
            }
            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el PDF", e);
        }
    }

    private int fila(Sheet s, int n, String a, Object b) {
        Row r = s.createRow(n);
        r.createCell(0).setCellValue(a);
        Cell c = r.createCell(1);
        if (b instanceof Number num) c.setCellValue(num.doubleValue()); else c.setCellValue(String.valueOf(b));
        return n + 1;
    }

    private String fecha(ReporteVentasDTO r) { return DF.format(r.getDesde()); }
    private String dinero(BigDecimal v) { return "S/ " + (v == null ? "0.00" : v.setScale(2, RoundingMode.HALF_UP).toPlainString()); }

    private void addHeader(PdfPTable table, String... values) {
        for (String value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value, new Font(Font.HELVETICA, 8, Font.BOLD)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }
    private void addPair(PdfPTable t, String a, String b, String c, String d) {
        addCell(t, a, new Font(Font.HELVETICA, 8, Font.BOLD)); addCell(t, b, new Font(Font.HELVETICA, 8));
        addCell(t, c, new Font(Font.HELVETICA, 8, Font.BOLD)); addCell(t, d, new Font(Font.HELVETICA, 8));
    }
    private void addCell(PdfPTable t, String value, Font font) { t.addCell(new PdfPCell(new Phrase(value == null ? "-" : value, font))); }
}

package com.transmaqsur.sigem.reporte;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Exporta un {@link Reporte} a Excel (.xlsx) con Apache POI. */
@Component
public class ExportadorExcel {

    public byte[] exportar(Reporte reporte) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet hoja = wb.createSheet(reporte.titulo().length() > 30 ? reporte.titulo().substring(0, 30) : reporte.titulo());

            Font negrita = wb.createFont();
            negrita.setBold(true);
            Font blanca = wb.createFont();
            blanca.setBold(true);
            blanca.setColor(IndexedColors.WHITE.getIndex());

            CellStyle titulo = wb.createCellStyle();
            Font fTitulo = wb.createFont();
            fTitulo.setBold(true);
            fTitulo.setFontHeightInPoints((short) 14);
            titulo.setFont(fTitulo);

            CellStyle cabecera = wb.createCellStyle();
            cabecera.setFont(blanca);
            cabecera.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            cabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            bordes(cabecera);

            CellStyle normal = wb.createCellStyle();
            bordes(normal);
            CellStyle numero = wb.createCellStyle();
            bordes(numero);
            numero.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
            CellStyle total = wb.createCellStyle();
            bordes(total);
            total.setFont(negrita);
            total.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));

            int r = 0;
            celda(hoja.createRow(r++), 0, "TRANSMAQ SUR S.A.C. - SIGEM", titulo);
            celda(hoja.createRow(r++), 0, reporte.titulo(), titulo);
            celda(hoja.createRow(r++), 0, reporte.subtitulo() + " | Generado: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), null);
            r++;

            Row head = hoja.createRow(r++);
            for (int c = 0; c < reporte.columnas().size(); c++) {
                celda(head, c, reporte.columnas().get(c), cabecera);
            }
            for (List<Object> fila : reporte.filas()) {
                Row row = hoja.createRow(r++);
                for (int c = 0; c < fila.size(); c++) {
                    Object v = fila.get(c);
                    celda(row, c, v, v instanceof Number ? numero : normal);
                }
            }
            if (reporte.tieneTotales()) {
                Row row = hoja.createRow(r);
                for (int c = 0; c < reporte.totales().size(); c++) {
                    celda(row, c, reporte.totales().get(c), total);
                }
            }
            for (int c = 0; c < reporte.columnas().size(); c++) {
                hoja.autoSizeColumn(c);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void celda(Row row, int col, Object valor, CellStyle estilo) {
        Cell cell = row.createCell(col);
        if (valor instanceof BigDecimal b) {
            cell.setCellValue(b.doubleValue());
        } else if (valor instanceof Number n) {
            cell.setCellValue(n.doubleValue());
        } else {
            cell.setCellValue(valor == null ? "" : valor.toString());
        }
        if (estilo != null) {
            cell.setCellStyle(estilo);
        }
    }

    private static void bordes(CellStyle s) {
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }
}

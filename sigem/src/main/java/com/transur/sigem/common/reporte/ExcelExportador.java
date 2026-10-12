package com.transur.sigem.common.reporte;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Component
public class ExcelExportador implements ExportadorReporte {

    @Override public String formato() { return "excel"; }
    @Override public String extension() { return "xlsx"; }
    @Override public String contentType() { return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"; }

    @Override
    public byte[] exportar(TablaReporte tabla) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet hoja = wb.createSheet("Reporte");
            Font negrita = wb.createFont();
            negrita.setBold(true);
            CellStyle titulo = wb.createCellStyle();
            Font grande = wb.createFont();
            grande.setBold(true);
            grande.setFontHeightInPoints((short) 14);
            titulo.setFont(grande);
            CellStyle cabecera = wb.createCellStyle();
            cabecera.setFont(negrita);
            cabecera.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cabecera.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cabecera.setBorderBottom(BorderStyle.THIN);

            Cell t = hoja.createRow(0).createCell(0);
            t.setCellValue(tabla.titulo());
            t.setCellStyle(titulo);
            hoja.createRow(1).createCell(0).setCellValue(tabla.subtitulo());

            Row head = hoja.createRow(3);
            for (int c = 0; c < tabla.columnas().size(); c++) {
                Cell cell = head.createCell(c);
                cell.setCellValue(tabla.columnas().get(c));
                cell.setCellStyle(cabecera);
            }
            int r = 4;
            for (List<Object> fila : tabla.filas()) {
                Row row = hoja.createRow(r++);
                for (int c = 0; c < fila.size(); c++) {
                    Object v = fila.get(c);
                    Cell cell = row.createCell(c);
                    if (v instanceof Number n) cell.setCellValue(n.doubleValue());
                    else cell.setCellValue(v == null ? "" : v.toString());
                }
            }
            for (int c = 0; c < tabla.columnas().size(); c++) hoja.autoSizeColumn(c);
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}

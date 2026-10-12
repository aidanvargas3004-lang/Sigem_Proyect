package com.transur.sigem.common.reporte;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class PdfExportador implements ExportadorReporte {

    @Override public String formato() { return "pdf"; }
    @Override public String extension() { return "pdf"; }
    @Override public String contentType() { return "application/pdf"; }

    @Override
    public byte[] exportar(TablaReporte tabla) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(tabla.columnas().size() > 6 ? PageSize.A4.rotate() : PageSize.A4, 28, 28, 30, 30);
        PdfWriter.getInstance(doc, out);
        doc.open();
        doc.add(new Paragraph("TRANSUR S.A.C. · SIGEM", FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY)));
        doc.add(new Paragraph(tabla.titulo(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15)));
        doc.add(new Paragraph(tabla.subtitulo() + " · Generado: "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY)));
        doc.add(Chunk.NEWLINE);

        PdfPTable t = new PdfPTable(tabla.columnas().size());
        t.setWidthPercentage(100);
        t.setHeaderRows(1);
        Font cab = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
        Font cuerpo = FontFactory.getFont(FontFactory.HELVETICA, 8);
        for (String c : tabla.columnas()) {
            PdfPCell cell = new PdfPCell(new Phrase(c, cab));
            cell.setBackgroundColor(new Color(30, 41, 59));
            cell.setPadding(5);
            t.addCell(cell);
        }
        boolean par = false;
        for (List<Object> fila : tabla.filas()) {
            for (Object v : fila) {
                PdfPCell cell = new PdfPCell(new Phrase(v == null ? "" : v.toString(), cuerpo));
                cell.setPadding(4);
                if (par) cell.setBackgroundColor(new Color(248, 250, 252));
                if (v instanceof Number) cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                t.addCell(cell);
            }
            par = !par;
        }
        if (tabla.filas().isEmpty()) {
            PdfPCell cell = new PdfPCell(new Phrase("Sin registros para los filtros seleccionados", cuerpo));
            cell.setColspan(tabla.columnas().size());
            cell.setPadding(8);
            t.addCell(cell);
        }
        doc.add(t);
        doc.close();
        return out.toByteArray();
    }
}

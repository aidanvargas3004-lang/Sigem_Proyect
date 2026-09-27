package com.transmaqsur.sigem.reporte;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Exporta un {@link Reporte} a PDF (A4 horizontal) con OpenPDF. */
@Component
public class ExportadorPdf {

    private static final Color AZUL = new Color(30, 41, 59);
    private static final Color GRIS = new Color(241, 245, 249);

    public byte[] exportar(Reporte reporte) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 28, 28, 28, 28);
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font fEmpresa = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(217, 119, 6));
        Font fTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15, AZUL);
        Font fSub = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);
        Font fHead = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.WHITE);
        Font fCelda = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
        Font fTotal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);

        doc.add(new Paragraph("TRANSMAQ SUR S.A.C. · Sistema Integral de Gestión de Flotas y Maquinaria", fEmpresa));
        doc.add(new Paragraph(reporte.titulo(), fTitulo));
        doc.add(new Paragraph(reporte.subtitulo() + "   |   Generado: "
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fSub));
        doc.add(new Paragraph(" "));

        PdfPTable tabla = new PdfPTable(reporte.columnas().size());
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);
        for (String col : reporte.columnas()) {
            PdfPCell c = new PdfPCell(new Phrase(col, fHead));
            c.setBackgroundColor(AZUL);
            c.setPadding(4);
            tabla.addCell(c);
        }
        int i = 0;
        for (List<Object> fila : reporte.filas()) {
            boolean par = i++ % 2 == 0;
            for (Object v : fila) {
                tabla.addCell(celda(v, fCelda, par ? null : GRIS));
            }
        }
        if (reporte.tieneTotales()) {
            for (Object v : reporte.totales()) {
                tabla.addCell(celda(v, fTotal, new Color(254, 243, 199)));
            }
        }
        doc.add(tabla);
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Registros: " + reporte.filas().size(), fSub));
        doc.close();
        return out.toByteArray();
    }

    private static PdfPCell celda(Object v, Font f, Color fondo) {
        PdfPCell c = new PdfPCell(new Phrase(formatear(v), f));
        c.setPadding(3);
        if (v instanceof Number) {
            c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        }
        if (fondo != null) {
            c.setBackgroundColor(fondo);
        }
        return c;
    }

    static String formatear(Object v) {
        if (v == null) {
            return "";
        }
        if (v instanceof BigDecimal || v instanceof Double) {
            return new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US)).format(v);
        }
        return v.toString();
    }
}

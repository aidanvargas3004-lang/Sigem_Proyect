package com.transur.sigem.common;

import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.common.reporte.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** RF017: exportación de reportes a Excel y PDF mediante estrategias intercambiables. */
class ExportadoresTest {

    private final TablaReporte tabla = new TablaReporte("Prueba", "Período de prueba",
            List.of("Unidad", "Horas"), List.of(List.of("EXC-01", 8.5), List.of("VOL-02", 3)));
    private final ExportadorReporteFactory factory =
            new ExportadorReporteFactory(List.of(new ExcelExportador(), new PdfExportador()));

    @Test
    void excelGeneraUnArchivoXlsxValido() {
        byte[] xlsx = new ExcelExportador().exportar(tabla);
        assertThat(new String(xlsx, 0, 2, StandardCharsets.ISO_8859_1)).isEqualTo("PK");
    }

    @Test
    void pdfGeneraUnDocumentoValido() {
        byte[] pdf = new PdfExportador().exportar(tabla);
        assertThat(new String(pdf, 0, 4, StandardCharsets.ISO_8859_1)).isEqualTo("%PDF");
    }

    @Test
    void laFabricaArmaLaDescargaConNombreYTipo() {
        ResponseEntity<byte[]> r = factory.descargar("pdf", "reporte_flota", tabla);
        assertThat(r.getHeaders().getContentType().toString()).isEqualTo("application/pdf");
        assertThat(r.getHeaders().getContentDisposition().getFilename()).startsWith("reporte_flota_").endsWith(".pdf");
    }

    @Test
    void formatoNoSoportadoEsUnErrorDeNegocio() {
        assertThatThrownBy(() -> factory.descargar("csv", "x", tabla)).isInstanceOf(NegocioException.class);
    }
}

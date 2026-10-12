package com.transur.sigem.common.reporte;

import com.transur.sigem.common.exception.NegocioException;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Selecciona el exportador según el formato pedido y arma la respuesta de descarga. */
@Component
public class ExportadorReporteFactory {

    private final Map<String, ExportadorReporte> exportadores;

    public ExportadorReporteFactory(List<ExportadorReporte> exportadores) {
        this.exportadores = exportadores.stream().collect(Collectors.toMap(ExportadorReporte::formato, Function.identity()));
    }

    public ResponseEntity<byte[]> descargar(String formato, String nombreBase, TablaReporte tabla) {
        ExportadorReporte e = exportadores.get(formato);
        if (e == null) throw new NegocioException("Formato de exportación no soportado: " + formato);
        String archivo = nombreBase + "_" + LocalDate.now() + "." + e.extension();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(archivo).build().toString())
                .contentType(MediaType.parseMediaType(e.contentType()))
                .body(e.exportar(tabla));
    }
}

package com.transmaqsur.sigem.reporte;

import java.util.List;

/**
 * Reporte tabular genérico que puede mostrarse en pantalla o exportarse a Excel y PDF.
 *
 * @param totales fila opcional de totales (misma cantidad de columnas; null en celdas vacías)
 */
public record Reporte(String clave, String titulo, String subtitulo, List<String> columnas,
                      List<List<Object>> filas, List<Object> totales) {

    public boolean tieneTotales() {
        return totales != null && !totales.isEmpty();
    }
}

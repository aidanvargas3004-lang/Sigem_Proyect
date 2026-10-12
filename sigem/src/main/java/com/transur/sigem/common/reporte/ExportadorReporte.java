package com.transur.sigem.common.reporte;

/**
 * Estrategia de exportación (patrón Strategy, principio abierto/cerrado):
 * para un nuevo formato basta con agregar otra implementación.
 */
public interface ExportadorReporte {

    /** Identificador del formato usado en la URL: excel, pdf. */
    String formato();

    String extension();

    String contentType();

    byte[] exportar(TablaReporte tabla);
}

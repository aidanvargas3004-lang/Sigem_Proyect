package com.transur.sigem.logistica.dto;

import java.util.List;

/** Resumen de una importación de repuestos desde Excel. */
public record ResultadoImportacion(int creados, int actualizados, List<String> errores) {

    public int procesados() {
        return creados + actualizados;
    }
}

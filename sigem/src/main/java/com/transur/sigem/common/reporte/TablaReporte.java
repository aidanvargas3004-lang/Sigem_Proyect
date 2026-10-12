package com.transur.sigem.common.reporte;

import java.util.List;

/** Modelo neutro de un reporte tabular: lo producen los DAO/servicios y lo consumen los exportadores. */
public record TablaReporte(String titulo, String subtitulo, List<String> columnas, List<List<Object>> filas) {
}

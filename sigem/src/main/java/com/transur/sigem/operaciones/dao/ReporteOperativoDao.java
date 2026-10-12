package com.transur.sigem.operaciones.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DAO de reportes operativos (RF016). Consultas de agregación en SQL nativo mediante JDBC:
 * son de solo lectura, cruzan varias tablas y no necesitan el ciclo de vida de JPA.
 */
public interface ReporteOperativoDao {

    List<Map<String, Object>> serviciosRealizados(LocalDate desde, LocalDate hasta);

    List<Map<String, Object>> usoDeFlota(LocalDate desde, LocalDate hasta);

    List<Map<String, Object>> mantenimientos(LocalDate desde, LocalDate hasta);

    List<Map<String, Object>> incidencias(LocalDate desde, LocalDate hasta);

    List<Map<String, Object>> disponibilidad();

    /** Indicadores del período: servicios, horas, km, costo de mantenimiento, incidencias. */
    Map<String, Object> indicadores(LocalDate desde, LocalDate hasta);
}

package com.transmaqsur.sigem.dto;

import com.transmaqsur.sigem.model.Asignacion;
import com.transmaqsur.sigem.model.Contrato;
import com.transmaqsur.sigem.model.Inventario;
import com.transmaqsur.sigem.model.Unidad;
import com.transmaqsur.sigem.model.enums.EstadoUnidad;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO de solo lectura con los indicadores del panel principal (RF015).
 * Lo arma DashboardService y lo consume la vista dashboard.html.
 */
public record ResumenDashboardDTO(
        Map<EstadoUnidad, Long> unidadesPorEstado,
        long totalUnidades,
        long operacionesEnCurso,
        long operacionesProgramadas,
        long ordenesAbiertas,
        long solicitudesPendientes,
        BigDecimal ventasMes,
        BigDecimal montoPorFacturar,
        long operacionesPorFacturar,
        List<Unidad> mantenimientoProximo,
        List<Inventario> stockBajo,
        List<Contrato> contratosPorVencer,
        List<Asignacion> proximasAsignaciones,
        List<String> mesesLabels,
        List<BigDecimal> ventasPorMes,
        int disponibilidadPorcentaje,
        long serviciosHoy,
        long incidenciasAbiertas) {
}

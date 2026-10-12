package com.transur.sigem.operaciones.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ReporteOperativoJdbcDao implements ReporteOperativoDao {

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public List<Map<String, Object>> serviciosRealizados(LocalDate desde, LocalDate hasta) {
        return jdbc.queryForList("""
                SELECT s.id, s.fecha_programada, s.fecha_inicio_real, s.fecha_fin_real, cl.razon_social AS cliente,
                       ts.nombre AS tipo_servicio, s.origen, s.destino, s.estado,
                       (SELECT string_agg(DISTINCT m.codigo_interno, ', ') FROM asignaciones a JOIN maquinarias m ON m.id = a.maquinaria_id
                         WHERE a.servicio_id = s.id AND a.estado <> 'CANCELADA') AS unidades,
                       (SELECT string_agg(DISTINCT e.nombres || ' ' || e.apellidos, ', ') FROM asignaciones a JOIN empleados e ON e.id = a.empleado_id
                         WHERE a.servicio_id = s.id AND a.estado <> 'CANCELADA') AS operadores,
                       s.horas_uso, s.km_recorridos
                FROM servicios s
                JOIN contratos c ON c.id = s.contrato_id
                JOIN clientes cl ON cl.id = c.cliente_id
                JOIN tipos_servicio ts ON ts.id = s.tipo_servicio_id
                WHERE s.fecha_programada >= :desde AND s.fecha_programada < :hasta
                ORDER BY s.fecha_programada DESC
                """, rango(desde, hasta));
    }

    @Override
    public List<Map<String, Object>> usoDeFlota(LocalDate desde, LocalDate hasta) {
        return jdbc.queryForList("""
                SELECT m.codigo_interno, tm.nombre AS tipo, m.placa, m.estado_operativo,
                       COALESCE(sv.servicios, 0) AS servicios, COALESCE(sv.horas, 0) AS horas_uso, COALESCE(sv.km, 0) AS km_recorridos,
                       COALESCE(mt.mantenimientos, 0) AS mantenimientos, COALESCE(mt.costo, 0) AS costo_mantenimiento,
                       COALESCE(inc.incidencias, 0) AS incidencias, m.horometro_actual, m.kilometraje_actual
                FROM maquinarias m
                JOIN tipos_maquinaria tm ON tm.id = m.tipo_id
                LEFT JOIN (
                    SELECT a.maquinaria_id, COUNT(DISTINCT s.id) AS servicios, SUM(s.horas_uso) AS horas, SUM(s.km_recorridos) AS km
                    FROM asignaciones a JOIN servicios s ON s.id = a.servicio_id
                    WHERE s.estado = 'FINALIZADO' AND COALESCE(s.fecha_fin_real, s.fecha_programada) >= :desde
                      AND COALESCE(s.fecha_fin_real, s.fecha_programada) < :hasta
                    GROUP BY a.maquinaria_id) sv ON sv.maquinaria_id = m.id
                LEFT JOIN (
                    SELECT maquinaria_id, COUNT(*) AS mantenimientos, SUM(CASE WHEN estado = 'COMPLETADO' THEN costo ELSE 0 END) AS costo
                    FROM mantenimientos
                    WHERE COALESCE(fecha_fin, fecha_programada, fecha_inicio) >= CAST(:desde AS date)
                      AND COALESCE(fecha_fin, fecha_programada, fecha_inicio) < CAST(:hasta AS date)
                    GROUP BY maquinaria_id) mt ON mt.maquinaria_id = m.id
                LEFT JOIN (
                    SELECT maquinaria_id, COUNT(*) AS incidencias FROM incidencias
                    WHERE fecha >= :desde AND fecha < :hasta GROUP BY maquinaria_id) inc ON inc.maquinaria_id = m.id
                ORDER BY horas_uso DESC, m.codigo_interno
                """, rango(desde, hasta));
    }

    @Override
    public List<Map<String, Object>> mantenimientos(LocalDate desde, LocalDate hasta) {
        return jdbc.queryForList("""
                SELECT mt.id, m.codigo_interno, mt.tipo_mantenimiento, mt.estado, mt.fecha_programada, mt.fecha_inicio, mt.fecha_fin,
                       COALESCE(e.nombres || ' ' || e.apellidos, '-') AS tecnico, p.actividad AS plan, mt.costo,
                       (SELECT COUNT(*) FROM mantenimiento_repuestos r WHERE r.mantenimiento_id = mt.id) AS repuestos
                FROM mantenimientos mt
                JOIN maquinarias m ON m.id = mt.maquinaria_id
                LEFT JOIN empleados e ON e.id = mt.empleado_id
                LEFT JOIN planes_mantenimiento p ON p.id = mt.plan_id
                WHERE COALESCE(mt.fecha_programada, mt.fecha_inicio, mt.fecha_fin) >= CAST(:desde AS date)
                  AND COALESCE(mt.fecha_programada, mt.fecha_inicio, mt.fecha_fin) < CAST(:hasta AS date)
                ORDER BY COALESCE(mt.fecha_programada, mt.fecha_inicio) DESC, mt.id DESC
                """, rango(desde, hasta));
    }

    @Override
    public List<Map<String, Object>> incidencias(LocalDate desde, LocalDate hasta) {
        return jdbc.queryForList("""
                SELECT i.id, i.fecha, i.servicio_id, COALESCE(m.codigo_interno, '-') AS unidad, i.tipo, i.estado, i.descripcion
                FROM incidencias i LEFT JOIN maquinarias m ON m.id = i.maquinaria_id
                WHERE i.fecha >= :desde AND i.fecha < :hasta
                ORDER BY i.fecha DESC
                """, rango(desde, hasta));
    }

    @Override
    public List<Map<String, Object>> disponibilidad() {
        return jdbc.queryForList("""
                SELECT m.estado_operativo, COUNT(*) AS unidades,
                       ROUND(100.0 * COUNT(*) / NULLIF(SUM(COUNT(*)) OVER (), 0), 1) AS porcentaje,
                       string_agg(m.codigo_interno, ', ' ORDER BY m.codigo_interno) AS codigos
                FROM maquinarias m
                WHERE m.estado_operativo NOT IN ('VENDIDO', 'BAJA_DEFINITIVA')
                GROUP BY m.estado_operativo ORDER BY unidades DESC
                """, new MapSqlParameterSource());
    }

    @Override
    public Map<String, Object> indicadores(LocalDate desde, LocalDate hasta) {
        return jdbc.queryForMap("""
                SELECT
                  (SELECT COUNT(*) FROM servicios WHERE fecha_programada >= :desde AND fecha_programada < :hasta) AS servicios,
                  (SELECT COUNT(*) FROM servicios WHERE estado = 'FINALIZADO' AND fecha_programada >= :desde AND fecha_programada < :hasta) AS finalizados,
                  (SELECT COALESCE(SUM(horas_uso), 0) FROM servicios WHERE fecha_programada >= :desde AND fecha_programada < :hasta) AS horas,
                  (SELECT COALESCE(SUM(km_recorridos), 0) FROM servicios WHERE fecha_programada >= :desde AND fecha_programada < :hasta) AS km,
                  (SELECT COALESCE(SUM(costo), 0) FROM mantenimientos WHERE estado = 'COMPLETADO'
                     AND fecha_fin >= CAST(:desde AS date) AND fecha_fin < CAST(:hasta AS date)) AS costo_mantenimiento,
                  (SELECT COUNT(*) FROM incidencias WHERE fecha >= :desde AND fecha < :hasta) AS incidencias,
                  (SELECT ROUND(100.0 * COUNT(*) FILTER (WHERE estado_operativo IN ('OPERATIVO', 'EN_SERVICIO', 'ALQUILADA')) / NULLIF(COUNT(*), 0), 1)
                     FROM maquinarias WHERE estado_operativo NOT IN ('VENDIDO', 'BAJA_DEFINITIVA')) AS disponibilidad
                """, rango(desde, hasta));
    }

    /** Rango semiabierto [desde, hasta + 1 día). */
    private static MapSqlParameterSource rango(LocalDate desde, LocalDate hasta) {
        return new MapSqlParameterSource()
                .addValue("desde", Timestamp.valueOf(desde.atStartOfDay()))
                .addValue("hasta", Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
    }
}

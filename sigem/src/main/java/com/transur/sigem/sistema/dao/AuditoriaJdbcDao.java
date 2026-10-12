package com.transur.sigem.sistema.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class AuditoriaJdbcDao implements AuditoriaDao {

    private static final RowMapper<RegistroAuditoria> MAPPER = (rs, i) -> new RegistroAuditoria(
            rs.getLong("id"), rs.getString("username"), rs.getString("modulo"), rs.getString("accion"),
            rs.getString("entidad"), (Long) rs.getObject("entidad_id", Long.class),
            rs.getString("datos_anteriores"), rs.getString("datos_nuevos"),
            rs.getTimestamp("fecha").toLocalDateTime());

    private final JdbcTemplate jdbc;

    @Override
    public void insertar(String username, String modulo, String accion, String entidad, Long entidadId,
                         String datosAnterioresJson, String datosNuevosJson) {
        jdbc.update("""
                INSERT INTO auditoria (usuario_id, modulo, accion, entidad, entidad_id, datos_anteriores, datos_nuevos, fecha)
                VALUES ((SELECT id FROM usuarios WHERE username = ?), ?, ?, ?, ?, CAST(? AS jsonb), CAST(? AS jsonb), now())
                """, username, modulo, accion, entidad, entidadId, datosAnterioresJson, datosNuevosJson);
    }

    @Override
    public List<RegistroAuditoria> buscar(FiltroAuditoria f) {
        StringBuilder sql = new StringBuilder("""
                SELECT a.id, COALESCE(u.username, 'SISTEMA') AS username, a.modulo, a.accion, a.entidad, a.entidad_id,
                       CAST(a.datos_anteriores AS text) AS datos_anteriores, CAST(a.datos_nuevos AS text) AS datos_nuevos, a.fecha
                FROM auditoria a LEFT JOIN usuarios u ON u.id = a.usuario_id WHERE 1 = 1
                """);
        List<Object> params = new ArrayList<>();
        if (tiene(f.modulo())) { sql.append(" AND a.modulo = ?"); params.add(f.modulo()); }
        if (tiene(f.accion())) { sql.append(" AND a.accion = ?"); params.add(f.accion()); }
        if (tiene(f.usuario())) { sql.append(" AND u.username ILIKE ?"); params.add("%" + f.usuario().trim() + "%"); }
        if (tiene(f.entidad())) { sql.append(" AND a.entidad ILIKE ?"); params.add("%" + f.entidad().trim() + "%"); }
        if (f.desde() != null) { sql.append(" AND a.fecha >= ?"); params.add(Timestamp.valueOf(f.desde().atStartOfDay())); }
        if (f.hasta() != null) { sql.append(" AND a.fecha < ?"); params.add(Timestamp.valueOf(f.hasta().plusDays(1).atStartOfDay())); }
        sql.append(" ORDER BY a.fecha DESC, a.id DESC LIMIT ?");
        params.add(f.limite() > 0 ? f.limite() : 300);
        return jdbc.query(sql.toString(), MAPPER, params.toArray());
    }

    @Override
    public List<String> modulos() {
        return jdbc.queryForList("SELECT DISTINCT modulo FROM auditoria ORDER BY modulo", String.class);
    }

    @Override
    public Map<String, Long> resumenPorModulo(int dias) {
        Map<String, Long> res = new LinkedHashMap<>();
        jdbc.query("""
                SELECT modulo, COUNT(*) AS total FROM auditoria
                WHERE fecha >= now() - make_interval(days => ?) GROUP BY modulo ORDER BY total DESC
                """, rs -> { res.put(rs.getString("modulo"), rs.getLong("total")); }, dias);
        return res;
    }

    private static boolean tiene(String s) {
        return s != null && !s.isBlank();
    }
}

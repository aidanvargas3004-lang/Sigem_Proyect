package com.transmaqsur.sigem.dao;

import com.transmaqsur.sigem.model.enums.AccionAuditoria;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Implementación JDBC del DAO de auditoría (consulta parametrizada: sin riesgo de inyección SQL). */
@Repository
@RequiredArgsConstructor
public class JdbcAuditoriaDao implements AuditoriaDao {

    private static final String INSERTAR =
            "insert into auditoria (fecha, usuario, accion, entidad, entidad_id, detalle, ip) values (?,?,?,?,?,?,?)";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void insertar(LocalDateTime fecha, String usuario, AccionAuditoria accion, String entidad, Long entidadId,
                         String detalle, String ip) {
        jdbcTemplate.update(INSERTAR, Timestamp.valueOf(fecha), usuario, accion.name(), entidad, entidadId, detalle, ip);
    }
}

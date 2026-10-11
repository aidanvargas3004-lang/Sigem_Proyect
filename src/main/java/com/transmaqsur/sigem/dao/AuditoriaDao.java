package com.transmaqsur.sigem.dao;

import com.transmaqsur.sigem.model.enums.AccionAuditoria;

import java.time.LocalDateTime;

/**
 * DAO clásico (Data Access Object) para escribir la bitácora de auditoría.
 * Se usa JDBC directo porque se invoca desde los listeners de Hibernate, donde no
 * se puede usar el EntityManager. Las consultas usan la interfaz AuditoriaRepository.
 */
public interface AuditoriaDao {

    void insertar(LocalDateTime fecha, String usuario, AccionAuditoria accion, String entidad, Long entidadId,
                  String detalle, String ip);
}

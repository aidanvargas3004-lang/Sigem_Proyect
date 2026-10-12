package com.transur.sigem.sistema.dao;

import java.time.LocalDateTime;

/** DTO de lectura/escritura del DAO de auditoría (fila de la tabla auditoria). */
public record RegistroAuditoria(Long id, String usuario, String modulo, String accion, String entidad,
                                Long entidadId, String datosAnteriores, String datosNuevos, LocalDateTime fecha) {
}

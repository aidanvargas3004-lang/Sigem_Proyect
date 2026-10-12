package com.transur.sigem.sistema.dao;

import java.util.List;
import java.util.Map;

/**
 * DAO de la bitácora de auditoría. Se implementa con JDBC (no con JPA) porque se invoca
 * desde los eventos de Hibernate durante el flush, donde no se debe volver a usar el EntityManager.
 */
public interface AuditoriaDao {

    void insertar(String username, String modulo, String accion, String entidad, Long entidadId,
                  String datosAnterioresJson, String datosNuevosJson);

    List<RegistroAuditoria> buscar(FiltroAuditoria filtro);

    List<String> modulos();

    /** Cantidad de eventos por módulo en los últimos N días. */
    Map<String, Long> resumenPorModulo(int dias);
}

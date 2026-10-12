package com.transur.sigem.sistema.dao;

import java.time.LocalDate;

/** Criterios opcionales de búsqueda de la bitácora. */
public record FiltroAuditoria(String modulo, String accion, String usuario, String entidad,
                              LocalDate desde, LocalDate hasta, int limite) {
}

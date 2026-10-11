package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Tipos de incidencia que reportan conductores y operadores durante un servicio. */
@Getter
@RequiredArgsConstructor
public enum TipoIncidencia {
    MECANICA("Falla mecánica", true),
    ELECTRICA("Falla eléctrica", true),
    NEUMATICOS("Neumáticos / orugas", true),
    HIDRAULICA("Sistema hidráulico", true),
    ACCIDENTE("Accidente o siniestro", true),
    VIA_BLOQUEADA("Vía bloqueada / clima", false),
    ACCESO_OBRA("Acceso a obra / cliente", false),
    DOCUMENTACION("Documentación / control policial", false),
    OTRO("Otro", false);

    private final String label;
    /** Indica si normalmente requiere intervención de Mantenimiento. */
    private final boolean afectaUnidad;
}

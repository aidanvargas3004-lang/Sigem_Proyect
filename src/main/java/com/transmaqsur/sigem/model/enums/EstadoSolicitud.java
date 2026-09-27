package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoSolicitud {
    REGISTRADA("Registrada"),
    COTIZADA("Cotizada"),
    APROBADA("Aprobada"),
    RECHAZADA("Rechazada"),
    ATENDIDA("Atendida"),
    ANULADA("Anulada");

    private final String label;
}

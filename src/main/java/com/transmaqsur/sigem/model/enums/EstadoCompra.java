package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoCompra {
    PENDIENTE("Pendiente"),
    APROBADA("Aprobada"),
    RECIBIDA("Recibida"),
    ANULADA("Anulada");

    private final String label;
}

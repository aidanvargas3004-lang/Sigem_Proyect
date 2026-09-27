package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoCotizacion {
    BORRADOR("Borrador"),
    ENVIADA("Enviada"),
    ACEPTADA("Aceptada"),
    RECHAZADA("Rechazada"),
    VENCIDA("Vencida");

    private final String label;
}

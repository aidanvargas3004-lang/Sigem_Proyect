package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoVenta {
    BORRADOR("Borrador"),
    EMITIDA("Emitida"),
    PAGADA("Pagada"),
    ANULADA("Anulada");

    private final String label;
}

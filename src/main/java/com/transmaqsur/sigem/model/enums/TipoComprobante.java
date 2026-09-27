package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoComprobante {
    FACTURA("Factura", "F001"),
    BOLETA("Boleta de venta", "B001");

    private final String label;
    private final String serie;
}

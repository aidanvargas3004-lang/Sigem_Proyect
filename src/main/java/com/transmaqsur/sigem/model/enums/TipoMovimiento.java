package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoMovimiento {
    ENTRADA("Entrada"),
    SALIDA("Salida"),
    AJUSTE_POSITIVO("Ajuste (+)"),
    AJUSTE_NEGATIVO("Ajuste (-)");

    private final String label;
}

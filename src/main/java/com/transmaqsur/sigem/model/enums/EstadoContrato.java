package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoContrato {
    VIGENTE("Vigente"),
    FINALIZADO("Finalizado"),
    RENOVADO("Renovado"),
    ANULADO("Anulado");

    private final String label;
}

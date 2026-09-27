package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoDocumento {
    RUC("RUC"),
    DNI("DNI"),
    CE("Carné de extranjería");

    private final String label;
}

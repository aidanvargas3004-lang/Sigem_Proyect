package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoServicio {
    ALQUILER("Alquiler de maquinaria"),
    TRANSPORTE("Transporte de carga");

    private final String label;
}

package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Prioridad {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta"),
    CRITICA("Crítica");

    private final String label;
}

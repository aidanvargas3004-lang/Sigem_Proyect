package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoOperacion {
    PROGRAMADO("Programado"),
    EN_CURSO("En curso"),
    FINALIZADO("Finalizado"),
    ANULADO("Anulado");

    private final String label;
}

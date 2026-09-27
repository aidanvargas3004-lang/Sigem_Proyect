package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoOrden {
    PROGRAMADA("Programada"),
    EN_PROCESO("En proceso"),
    COMPLETADA("Completada"),
    CANCELADA("Cancelada");

    private final String label;
}

package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoNotificacion {
    INFO("Información"),
    EXITO("Éxito"),
    ALERTA("Alerta"),
    PELIGRO("Crítico");

    private final String label;
}

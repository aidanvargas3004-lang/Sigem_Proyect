package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AreaEmpresa {
    OPERACIONES("Operaciones"),
    MANTENIMIENTO("Mantenimiento"),
    ADMINISTRACION_LOGISTICA("Administración y Logística"),
    COMERCIAL("Comercial"),
    RECURSOS_HUMANOS("Recursos Humanos"),
    GERENCIA("Gerencia");

    private final String label;
}

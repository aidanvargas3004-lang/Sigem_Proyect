package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CargoEmpleado {
    CONDUCTOR("Conductor"),
    OPERADOR("Operador de maquinaria"),
    TECNICO("Técnico de mantenimiento"),
    JEFE_OPERACIONES("Jefe de Operaciones"),
    JEFE_MANTENIMIENTO("Jefe de Mantenimiento"),
    ASISTENTE_ADMINISTRATIVO("Asistente administrativo"),
    COORDINADOR_COMERCIAL("Coordinador comercial"),
    ANALISTA_RRHH("Analista de RR.HH."),
    ALMACENERO("Almacenero"),
    GERENTE("Gerente"),
    OTRO("Otro");

    private final String label;
}

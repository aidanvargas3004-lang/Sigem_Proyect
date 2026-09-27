package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccionAuditoria {
    CREAR("Creación"),
    ACTUALIZAR("Actualización"),
    ELIMINAR("Eliminación"),
    INICIO_SESION("Inicio de sesión"),
    ACCESO_FALLIDO("Acceso fallido"),
    OPERACION("Operación de negocio");

    private final String label;
}

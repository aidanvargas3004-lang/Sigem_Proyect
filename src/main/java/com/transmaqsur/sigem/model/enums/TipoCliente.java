package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoCliente {
    CONSTRUCTORA("Empresa constructora"),
    MINERA("Contratista minero"),
    GOBIERNO_LOCAL("Gobierno local"),
    EMPRESA_PRIVADA("Empresa privada"),
    PERSONA_NATURAL("Persona natural");

    private final String label;
}

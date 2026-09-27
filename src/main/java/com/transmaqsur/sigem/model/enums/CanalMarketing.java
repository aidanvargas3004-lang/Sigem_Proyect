package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CanalMarketing {
    CORREO("Correo electrónico"),
    REDES_SOCIALES("Redes sociales"),
    VISITA_COMERCIAL("Visita comercial"),
    FERIA("Feria / evento"),
    LLAMADA("Llamada telefónica"),
    WEB("Página web"),
    REFERIDO("Referido");

    private final String label;
}

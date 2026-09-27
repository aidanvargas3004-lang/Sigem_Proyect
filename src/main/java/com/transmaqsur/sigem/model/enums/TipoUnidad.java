package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Tipos de unidades de la flota. Los vehículos miden su uso en kilómetros
 * (odómetro) y la maquinaria en horas (horómetro).
 */
@Getter
@RequiredArgsConstructor
public enum TipoUnidad {
    CAMION_CARGA("Camión de carga", true),
    VOLQUETE("Volquete", true),
    CISTERNA("Cisterna", true),
    EXCAVADORA("Excavadora", false),
    CARGADOR_FRONTAL("Cargador frontal", false),
    RETROEXCAVADORA("Retroexcavadora", false),
    COMPACTADORA("Compactadora", false);

    private final String label;
    private final boolean vehiculo;

    public String getMedida() {
        return vehiculo ? "km" : "h";
    }

    public String getNombreMedida() {
        return vehiculo ? "Kilometraje (odómetro)" : "Horas de uso (horómetro)";
    }
}

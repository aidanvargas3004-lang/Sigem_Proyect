package com.transmaqsur.sigem.dto;

/** DTO de entrada de la API GPS: lo que envía el dispositivo o la app del conductor. */
public record PosicionGpsRequest(String codigoUnidad, Double latitud, Double longitud, Double velocidad, String referencia) {
}

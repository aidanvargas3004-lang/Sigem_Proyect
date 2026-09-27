package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.model.enums.EstadoAsignacion;
import com.transmaqsur.sigem.model.enums.EstadoOrden;

import java.util.List;

/** Conjuntos de estados usados en varias consultas. */
final class EstadosActivos {

    static final List<EstadoAsignacion> ASIGNACION = List.of(EstadoAsignacion.PROGRAMADA, EstadoAsignacion.EN_CURSO);
    static final List<EstadoOrden> ORDEN = List.of(EstadoOrden.PROGRAMADA, EstadoOrden.EN_PROCESO);

    private EstadosActivos() {
    }
}

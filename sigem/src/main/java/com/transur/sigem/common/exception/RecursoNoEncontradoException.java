package com.transur.sigem.common.exception;

/** El registro solicitado no existe. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String entidad, Object id) {
        super(entidad + " no encontrado(a) con ID: " + id);
    }
}

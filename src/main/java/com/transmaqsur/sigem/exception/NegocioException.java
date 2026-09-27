package com.transmaqsur.sigem.exception;

/** Error de regla de negocio: se muestra al usuario tal cual. */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}

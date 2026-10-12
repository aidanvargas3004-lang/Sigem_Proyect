package com.transur.sigem.common.exception;

/** Regla de negocio incumplida. El mensaje se muestra tal cual al usuario. */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}

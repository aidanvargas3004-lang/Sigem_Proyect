package com.transmaqsur.sigem.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NoEncontradoException extends RuntimeException {

    public NoEncontradoException(String entidad, Long id) {
        super(entidad + " con id " + id + " no existe");
    }
}

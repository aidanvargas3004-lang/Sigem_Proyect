package com.transur.sigem.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Respuestas de error uniformes para la API REST (/api/**): nunca expone trazas
 * internas y usa el código HTTP correcto según el tipo de error.
 */
@RestControllerAdvice(annotations = org.springframework.web.bind.annotation.RestController.class)
public class ApiExceptionHandler {

    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<Map<String, Object>> negocio(NegocioException e) {
        return cuerpo(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), null);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException e) {
        return cuerpo(HttpStatus.NOT_FOUND, e.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            campos.put(fe.getField(), fe.getDefaultMessage());
        }
        return cuerpo(HttpStatus.BAD_REQUEST, "Datos inválidos", campos);
    }

    private ResponseEntity<Map<String, Object>> cuerpo(HttpStatus status, String mensaje, Object detalle) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("fecha", LocalDateTime.now().toString());
        body.put("estado", status.value());
        body.put("error", mensaje);
        if (detalle != null) {
            body.put("detalle", detalle);
        }
        return ResponseEntity.status(status).body(body);
    }
}

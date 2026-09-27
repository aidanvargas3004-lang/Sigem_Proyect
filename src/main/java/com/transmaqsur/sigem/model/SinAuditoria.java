package com.transmaqsur.sigem.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marca entidades de alto volumen cuyos cambios no se registran en la auditoría detallada. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SinAuditoria {
}

package com.transmaqsur.sigem.util;

import com.transmaqsur.sigem.model.EntidadBase;
import org.springframework.beans.BeanUtils;

import java.util.Arrays;
import java.util.stream.Stream;

public final class Entidades {

    private Entidades() {
    }

    /**
     * Copia los datos editables de un formulario sobre la entidad persistida,
     * ignorando los campos del sistema y los indicados (estado, detalles, etc.).
     */
    public static void copiar(Object origen, Object destino, String... ignorar) {
        String[] todos = Stream.concat(Arrays.stream(EntidadBase.CAMPOS_SISTEMA), Arrays.stream(ignorar))
                .toArray(String[]::new);
        BeanUtils.copyProperties(origen, destino, todos);
    }

    public static String codigo(String prefijo, Long id) {
        return String.format("%s-%05d", prefijo, id);
    }
}

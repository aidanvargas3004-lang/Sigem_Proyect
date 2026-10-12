package com.transur.sigem.sistema;

import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.operaciones.entity.Servicio;
import com.transur.sigem.sistema.audit.AuditoriaEntidadesListener;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/** RF018: el módulo se deduce del paquete y las relaciones se registran solo por su id. */
class AuditoriaEntidadesListenerTest {

    private static Object invocar(String metodo, Object arg) throws Exception {
        Method m = AuditoriaEntidadesListener.class.getDeclaredMethod(metodo, Object.class);
        m.setAccessible(true);
        return m.invoke(null, arg);
    }

    @Test
    void elModuloSeObtieneDelPaqueteDeLaEntidad() throws Exception {
        assertThat(invocar("modulo", new Servicio())).isEqualTo("OPERACIONES");
        assertThat(invocar("modulo", new Maquinaria())).isEqualTo("MAQUINARIA");
    }

    @Test
    void unaRelacionSeGuardaComoSuIdentificador() throws Exception {
        Maquinaria m = new Maquinaria();
        m.setId(42L);
        assertThat(invocar("simple", m)).isEqualTo(42L);
        assertThat(invocar("simple", "texto")).isEqualTo("texto");
        assertThat(invocar("simple", null)).isNull();
    }

    @Test
    void losTextosLargosSeRecortan() throws Exception {
        String largo = "x".repeat(800);
        assertThat(((String) invocar("simple", largo))).hasSize(501);
    }
}

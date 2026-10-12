package com.transur.sigem.sistema;

import com.transur.sigem.common.exception.RecursoNoEncontradoException;
import com.transur.sigem.sistema.backup.RespaldoInfo;
import com.transur.sigem.sistema.backup.RespaldoService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Respaldo: solo se descargan archivos con el nombre esperado (sin recorrer otras carpetas). */
class RespaldoServiceTest {

    private final RespaldoService service = new RespaldoService(null, null, null);

    @Test
    void rechazaNombresQueNoSonRespaldos() {
        assertThatThrownBy(() -> service.archivo("../application.properties")).isInstanceOf(RecursoNoEncontradoException.class);
        assertThatThrownBy(() -> service.archivo("sigem_db_2026.sql")).isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void muestraElTamanoEnUnidadesLegibles() {
        assertThat(new RespaldoInfo("a", 512, LocalDateTime.now()).getTamano()).isEqualTo("512 B");
        assertThat(new RespaldoInfo("a", 2048, LocalDateTime.now()).getTamano()).contains("KB");
        assertThat(new RespaldoInfo("a", 3 * 1024 * 1024, LocalDateTime.now()).getTamano()).contains("MB");
    }
}

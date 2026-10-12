package com.transur.sigem.operaciones;

import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.common.reporte.TablaReporte;
import com.transur.sigem.operaciones.dao.ReporteOperativoDao;
import com.transur.sigem.operaciones.service.ReporteOperativoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/** RF016: el servicio de reportes arma la tabla a partir del DAO y valida el período. */
@ExtendWith(MockitoExtension.class)
class ReporteOperativoServiceTest {

    @Mock ReporteOperativoDao dao;
    @InjectMocks ReporteOperativoService service;

    @Test
    void armaReporteDeIncidenciasConFormatoDeFecha() {
        LocalDate d = LocalDate.of(2026, 10, 1), h = LocalDate.of(2026, 10, 31);
        Map<String, Object> fila = new HashMap<>();
        fila.put("id", 3L);
        fila.put("fecha", Timestamp.valueOf(LocalDateTime.of(2026, 10, 5, 9, 30)));
        fila.put("servicio_id", 8L);
        fila.put("unidad", "EXC-01");
        fila.put("tipo", "MECANICA");
        fila.put("estado", "ABIERTA");
        fila.put("descripcion", "Fuga hidráulica");
        when(dao.incidencias(d, h)).thenReturn(List.of(fila));

        TablaReporte t = service.generar("incidencias", d, h);

        assertThat(t.titulo()).isEqualTo("Incidencias");
        assertThat(t.columnas()).hasSize(7);
        assertThat(t.filas()).hasSize(1);
        assertThat(t.filas().get(0).get(1)).isEqualTo("05/10/2026 09:30");
        assertThat(t.subtitulo()).contains("01/10/2026").contains("31/10/2026");
    }

    @Test
    void redondeaDecimalesADosPosiciones() {
        Map<String, Object> fila = new HashMap<>();
        fila.put("estado_operativo", "OPERATIVO");
        fila.put("unidades", 4L);
        fila.put("porcentaje", new BigDecimal("66.6666"));
        fila.put("codigos", "A, B, C, D");
        when(dao.disponibilidad()).thenReturn(List.of(fila));
        TablaReporte t = service.generar("disponibilidad", LocalDate.now(), LocalDate.now());
        assertThat(t.filas().get(0).get(2)).isEqualTo(new BigDecimal("66.67"));
    }

    @Test
    void rechazaPeriodoInvertidoOTipoDesconocido() {
        assertThatThrownBy(() -> service.generar("servicios", LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(NegocioException.class);
        assertThatThrownBy(() -> service.generar("otro", LocalDate.now(), LocalDate.now()))
                .hasMessageContaining("no válido");
    }
}

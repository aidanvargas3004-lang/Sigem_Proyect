package com.transur.sigem.operaciones;

import com.transur.sigem.comercial.entity.Contrato;
import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.operaciones.entity.Asignacion;
import com.transur.sigem.operaciones.entity.Servicio;
import com.transur.sigem.operaciones.entity.TipoServicio;
import com.transur.sigem.operaciones.repository.AsignacionRepository;
import com.transur.sigem.operaciones.repository.ServicioRepository;
import com.transur.sigem.operaciones.repository.TipoServicioRepository;
import com.transur.sigem.operaciones.service.ServicioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/** RF010: registro de inicio y cierre con cálculo de horas y kilómetros de uso. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServicioServiceEjecucionTest {

    @Mock ServicioRepository servicioRepository;
    @Mock TipoServicioRepository tipoRepository;
    @Mock ContratoRepository contratoRepository;
    @Mock AsignacionRepository asignacionRepository;
    @Mock MaquinariaRepository maquinariaRepository;
    @InjectMocks ServicioService servicioService;

    private Servicio servicio;
    private Maquinaria unidad;
    private Asignacion asignacion;

    @BeforeEach
    void setUp() {
        Contrato c = new Contrato();
        c.setNumero("CTR-1");
        TipoServicio t = new TipoServicio();
        t.setNombre("MOVIMIENTO_TIERRA");
        servicio = new Servicio();
        servicio.setId(1L);
        servicio.setContrato(c);
        servicio.setTipoServicio(t);
        servicio.setFechaProgramada(LocalDateTime.now().minusHours(10));
        servicio.setEstado("ASIGNADO");
        unidad = new Maquinaria();
        unidad.setId(7L);
        unidad.setCodigoInterno("EXC-07");
        unidad.setEstadoOperativo("OPERATIVO");
        unidad.setHorometroActual(new BigDecimal("1000"));
        unidad.setKilometrajeActual(new BigDecimal("5000"));
        asignacion = new Asignacion();
        asignacion.setServicio(servicio);
        asignacion.setMaquinaria(unidad);
        asignacion.setEstado("ACTIVA");
        when(servicioRepository.findById(1L)).thenReturn(Optional.of(servicio));
        when(asignacionRepository.findByServicioId(1L)).thenReturn(List.of(asignacion));
    }

    @Test
    void inicioYCierreCalculanUsoYLiberanLaUnidad() {
        LocalDateTime inicio = LocalDateTime.now().minusHours(9);
        servicioService.iniciar(1L, new BigDecimal("1000"), new BigDecimal("5000"), inicio);
        assertThat(servicio.getEstado()).isEqualTo("EN_CURSO");
        assertThat(unidad.getEstadoOperativo()).isEqualTo("EN_SERVICIO");

        when(asignacionRepository.findByMaquinariaIdAndEstado(7L, "ACTIVA")).thenReturn(List.of());
        servicioService.finalizar(1L, new BigDecimal("1008.5"), new BigDecimal("5120"), LocalDateTime.now(), "ok");

        assertThat(servicio.getEstado()).isEqualTo("FINALIZADO");
        assertThat(servicio.getHorasUso()).isEqualByComparingTo("8.5");
        assertThat(servicio.getKmRecorridos()).isEqualByComparingTo("120");
        assertThat(unidad.getHorometroActual()).isEqualByComparingTo("1008.5");
        assertThat(unidad.getEstadoOperativo()).isEqualTo("OPERATIVO");
        assertThat(asignacion.getEstado()).isEqualTo("COMPLETADA");
    }

    @Test
    void noPermiteLecturaInicialMenorQueLaDeLaUnidad() {
        assertThatThrownBy(() -> servicioService.iniciar(1L, new BigDecimal("900"), null, null))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("no pueden ser menores");
    }

    @Test
    void noPermiteHorometroFinalMenorQueElInicial() {
        servicioService.iniciar(1L, null, null, LocalDateTime.now().minusHours(2));
        assertThatThrownBy(() -> servicioService.finalizar(1L, new BigDecimal("999"), null, null, null))
                .isInstanceOf(NegocioException.class);
    }

    @Test
    void noIniciaSiLaUnidadEstaEnMantenimiento() {
        unidad.setEstadoOperativo("EN_MANTENIMIENTO");
        assertThatThrownBy(() -> servicioService.iniciar(1L, null, null, null)).hasMessageContaining("mantenimiento");
    }

    @Test
    void noFinalizaUnServicioQueNoEstaEnCurso() {
        assertThatThrownBy(() -> servicioService.finalizar(1L, BigDecimal.TEN, null, null, null))
                .hasMessageContaining("EN CURSO");
    }
}

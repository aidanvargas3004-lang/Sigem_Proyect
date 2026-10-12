package com.transur.sigem.operaciones;

import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.mantenimiento.entity.Mantenimiento;
import com.transur.sigem.mantenimiento.repository.MantenimientoRepository;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.operaciones.entity.Asignacion;
import com.transur.sigem.operaciones.entity.Servicio;
import com.transur.sigem.operaciones.repository.AsignacionRepository;
import com.transur.sigem.operaciones.service.ValidadorDisponibilidad;
import com.transur.sigem.rrhh.entity.DocumentoEmpleado;
import com.transur.sigem.rrhh.entity.Empleado;
import com.transur.sigem.rrhh.repository.DocumentoEmpleadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/** RF009: validación de conflictos al asignar unidad y operador. */
@ExtendWith(MockitoExtension.class)
class ValidadorDisponibilidadTest {

    @Mock AsignacionRepository asignacionRepository;
    @Mock MantenimientoRepository mantenimientoRepository;
    @Mock DocumentoEmpleadoRepository documentoRepository;
    @InjectMocks ValidadorDisponibilidad validador;

    private Maquinaria unidad;
    private Servicio servicio;

    @BeforeEach
    void setUp() {
        unidad = new Maquinaria();
        unidad.setId(10L);
        unidad.setCodigoInterno("EXC-01");
        unidad.setEstadoOperativo("OPERATIVO");
        servicio = servicio(1L, LocalDateTime.of(2026, 11, 3, 8, 0), LocalDateTime.of(2026, 11, 3, 18, 0));
    }

    private static Servicio servicio(Long id, LocalDateTime ini, LocalDateTime fin) {
        Servicio s = new Servicio();
        s.setId(id);
        s.setFechaProgramada(ini);
        s.setFechaFinProgramada(fin);
        return s;
    }

    private static Asignacion asignacion(Servicio s) {
        Asignacion a = new Asignacion();
        a.setServicio(s);
        a.setEstado("ACTIVA");
        return a;
    }

    @Test
    void unidadLibreSinMantenimientoEsAsignable() {
        when(asignacionRepository.findByMaquinariaIdAndEstado(10L, "ACTIVA")).thenReturn(List.of());
        when(mantenimientoRepository.findByMaquinariaIdAndEstadoInAndFechaProgramadaBetween(eq(10L), anyList(), any(), any()))
                .thenReturn(List.of());
        assertThatCode(() -> validador.validarUnidad(unidad, servicio)).doesNotThrowAnyException();
    }

    @Test
    void rechazaDobleAsignacionEnHorarioQueSeCruza() {
        Servicio otro = servicio(2L, LocalDateTime.of(2026, 11, 3, 15, 0), LocalDateTime.of(2026, 11, 3, 20, 0));
        when(asignacionRepository.findByMaquinariaIdAndEstado(10L, "ACTIVA")).thenReturn(List.of(asignacion(otro)));
        assertThatThrownBy(() -> validador.validarUnidad(unidad, servicio))
                .isInstanceOf(NegocioException.class)
                .hasMessageContaining("SER-2");
    }

    @Test
    void permiteServiciosConsecutivosSinCruce() {
        Servicio otro = servicio(2L, LocalDateTime.of(2026, 11, 3, 18, 0), LocalDateTime.of(2026, 11, 3, 22, 0));
        when(asignacionRepository.findByMaquinariaIdAndEstado(10L, "ACTIVA")).thenReturn(List.of(asignacion(otro)));
        when(mantenimientoRepository.findByMaquinariaIdAndEstadoInAndFechaProgramadaBetween(eq(10L), anyList(), any(), any()))
                .thenReturn(List.of());
        assertThatCode(() -> validador.validarUnidad(unidad, servicio)).doesNotThrowAnyException();
    }

    @Test
    void rechazaUnidadEnMantenimientoOVendida() {
        unidad.setEstadoOperativo("EN_MANTENIMIENTO");
        assertThatThrownBy(() -> validador.validarUnidad(unidad, servicio)).hasMessageContaining("mantenimiento");
        unidad.setEstadoOperativo("VENDIDO");
        assertThatThrownBy(() -> validador.validarUnidad(unidad, servicio)).hasMessageContaining("no puede asignarse");
    }

    @Test
    void rechazaUnidadConMantenimientoProgramadoEnElRango() {
        Mantenimiento m = new Mantenimiento();
        m.setFechaProgramada(LocalDate.of(2026, 11, 3));
        when(asignacionRepository.findByMaquinariaIdAndEstado(10L, "ACTIVA")).thenReturn(List.of());
        when(mantenimientoRepository.findByMaquinariaIdAndEstadoInAndFechaProgramadaBetween(eq(10L), anyList(), any(), any()))
                .thenReturn(List.of(m));
        assertThatThrownBy(() -> validador.validarUnidad(unidad, servicio)).hasMessageContaining("mantenimiento programado");
    }

    @Test
    void rechazaOperadorConLicenciaQueVenceAntesDelFin() {
        Empleado e = new Empleado();
        e.setId(5L);
        e.setNombres("Ana");
        e.setApellidos("Rojas");
        e.setCargo("CONDUCTOR");
        e.setEstado(true);
        DocumentoEmpleado lic = new DocumentoEmpleado();
        lic.setTipoDocumento("LICENCIA_A-IIIc");
        lic.setNumeroDocumento("Q123");
        lic.setFechaVencimiento(LocalDate.of(2026, 11, 1));
        lic.setEstado(true);
        when(asignacionRepository.findByEmpleadoIdAndEstado(5L, "ACTIVA")).thenReturn(List.of());
        when(documentoRepository.findByEmpleadoId(5L)).thenReturn(List.of(lic));
        assertThatThrownBy(() -> validador.validarOperador(e, servicio)).hasMessageContaining("Q123");
    }

    @Test
    void rechazaEmpleadoQueNoEsOperador() {
        Empleado e = new Empleado();
        e.setCargo("TECNICO");
        e.setEstado(true);
        assertThatThrownBy(() -> validador.validarOperador(e, servicio)).hasMessageContaining("no está habilitado");
    }
}

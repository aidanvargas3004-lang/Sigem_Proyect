package com.transur.sigem.mantenimiento;

import com.transur.sigem.mantenimiento.entity.PlanMantenimiento;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** RF012: el plan preventivo alerta al 90 % del intervalo y vence al 100 %. */
class PlanMantenimientoTest {

    private PlanMantenimiento plan(String criterio, int frecuencia, int ultimo, int lecturaActual) {
        Maquinaria m = new Maquinaria();
        m.setHorometroActual(BigDecimal.valueOf(lecturaActual));
        m.setKilometrajeActual(BigDecimal.valueOf(lecturaActual));
        PlanMantenimiento p = new PlanMantenimiento();
        p.setMaquinaria(m);
        p.setCriterio(criterio);
        p.setFrecuencia(BigDecimal.valueOf(frecuencia));
        p.setUltimoValor(BigDecimal.valueOf(ultimo));
        return p;
    }

    @Test
    void horometroPorDebajoDelUmbralEstaOk() {
        PlanMantenimiento p = plan(PlanMantenimiento.HOROMETRO, 250, 1000, 1200);
        assertThat(p.getPorcentaje()).isEqualTo(80);
        assertThat(p.getSituacion()).isEqualTo("OK");
        assertThat(p.getProximoValor()).isEqualByComparingTo("1250");
    }

    @Test
    void horometroAl90PorCientoEstaProximo() {
        assertThat(plan(PlanMantenimiento.HOROMETRO, 250, 1000, 1225).getSituacion()).isEqualTo("PROXIMO");
    }

    @Test
    void kilometrajeSuperadoEstaVencido() {
        PlanMantenimiento p = plan(PlanMantenimiento.KILOMETRAJE, 10000, 30000, 40500);
        assertThat(p.getSituacion()).isEqualTo("VENCIDO");
        assertThat(p.getUnidad()).isEqualTo("km");
    }

    @Test
    void planPorFechaCalculaProximaFechaYSituacion() {
        PlanMantenimiento p = plan(PlanMantenimiento.FECHA, 100, 0, 0);
        p.setUltimaFecha(LocalDate.now().minusDays(95));
        assertThat(p.getProximaFecha()).isEqualTo(LocalDate.now().plusDays(5));
        assertThat(p.getSituacion()).isEqualTo("PROXIMO");
        assertThat(p.getProximoValor()).isNull();
    }
}

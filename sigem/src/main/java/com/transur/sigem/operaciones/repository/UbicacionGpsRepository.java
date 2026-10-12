package com.transur.sigem.operaciones.repository;

import com.transur.sigem.operaciones.entity.UbicacionGps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UbicacionGpsRepository extends JpaRepository<UbicacionGps, Long> {

    List<UbicacionGps> findByServicioIdOrderByFechaHoraDesc(Long servicioId);

    List<UbicacionGps> findByMaquinariaIdOrderByFechaHoraAsc(Long maquinariaId);

    /** Última posición conocida de cada unidad. */
    @Query("select g from UbicacionGps g where g.fechaHora = "
            + "(select max(g2.fechaHora) from UbicacionGps g2 where g2.maquinaria = g.maquinaria)")
    List<UbicacionGps> ultimasPosiciones();
}

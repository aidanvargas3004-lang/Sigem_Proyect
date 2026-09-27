package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.UbicacionGps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UbicacionGpsRepository extends JpaRepository<UbicacionGps, Long> {

    Optional<UbicacionGps> findFirstByUnidadIdOrderByFechaHoraDesc(Long unidadId);

    /** Última posición conocida de cada unidad. */
    @Query("select g from UbicacionGps g where g.fechaHora = (select max(g2.fechaHora) from UbicacionGps g2 where g2.unidad = g.unidad) order by g.unidad.codigo")
    List<UbicacionGps> ultimasPosiciones();

    List<UbicacionGps> findByUnidadIdAndFechaHoraBetweenOrderByFechaHoraAsc(Long unidadId, LocalDateTime desde, LocalDateTime hasta);

    Page<UbicacionGps> findAllByOrderByFechaHoraDesc(Pageable pageable);
}

package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import com.transmaqsur.sigem.model.enums.EstadoUnidad;
import com.transmaqsur.sigem.model.enums.TipoUnidad;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UnidadRepository extends JpaRepository<Unidad, Long> {

    List<Unidad> findAllByOrderByCodigoAsc();

    List<Unidad> findByEstadoNotOrderByCodigoAsc(EstadoUnidad estado);

    List<Unidad> findByTipoInAndEstadoNotOrderByCodigoAsc(List<TipoUnidad> tipos, EstadoUnidad estado);

    Optional<Unidad> findByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    boolean existsByCodigo(String codigo);

    long countByEstado(EstadoUnidad estado);

    List<Unidad> findByVencimientoSoatBeforeOrVencimientoRevisionTecnicaBefore(LocalDate fecha1, LocalDate fecha2);
}

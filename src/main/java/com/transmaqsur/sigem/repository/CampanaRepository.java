package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Campana;
import org.springframework.data.jpa.repository.JpaRepository;
import com.transmaqsur.sigem.model.enums.EstadoCampana;

import java.util.List;

public interface CampanaRepository extends JpaRepository<Campana, Long> {

    List<Campana> findAllByOrderByFechaInicioDesc();

    List<Campana> findByEstadoInOrderByNombreAsc(List<EstadoCampana> estados);
}

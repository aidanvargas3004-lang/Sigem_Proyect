package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Repuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RepuestoRepository extends JpaRepository<Repuesto, Long> {

    List<Repuesto> findAllByOrderByNombreAsc();

    List<Repuesto> findByActivoTrueOrderByNombreAsc();

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    boolean existsByCodigo(String codigo);
}

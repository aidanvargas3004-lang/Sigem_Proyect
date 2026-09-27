package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Almacen;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlmacenRepository extends JpaRepository<Almacen, Long> {

    List<Almacen> findAllByOrderByNombreAsc();

    List<Almacen> findByActivoTrueOrderByNombreAsc();
}

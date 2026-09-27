package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.RepuestoUtilizado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RepuestoUtilizadoRepository extends JpaRepository<RepuestoUtilizado, Long> {

    List<RepuestoUtilizado> findAllByOrderByIdDesc();
}

package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Contacto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactoRepository extends JpaRepository<Contacto, Long> {

    List<Contacto> findByClienteIdOrderByNombresAsc(Long clienteId);
}

package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findAllByOrderByRazonSocialAsc();

    List<Cliente> findByActivoTrueOrderByRazonSocialAsc();

    boolean existsByNumeroDocumentoAndIdNot(String numeroDocumento, Long id);

    boolean existsByNumeroDocumento(String numeroDocumento);
}

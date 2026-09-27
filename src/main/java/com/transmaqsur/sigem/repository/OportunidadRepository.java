package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Oportunidad;
import org.springframework.data.jpa.repository.JpaRepository;
import com.transmaqsur.sigem.model.enums.EtapaOportunidad;

import java.util.List;

public interface OportunidadRepository extends JpaRepository<Oportunidad, Long> {

    List<Oportunidad> findAllByOrderByFechaCreacionDesc();

    List<Oportunidad> findByCampanaIdOrderByFechaCreacionDesc(Long campanaId);

    List<Oportunidad> findByClienteIdOrderByFechaCreacionDesc(Long clienteId);

    long countByEtapa(EtapaOportunidad etapa);
}

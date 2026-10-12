package com.transur.sigem.comercial.repository;
import com.transur.sigem.comercial.entity.Alquiler;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface AlquilerRepository extends JpaRepository<Alquiler, Long> {
    Optional<Alquiler> findByContratoId(Long contratoId);
    List<Alquiler> findByContratoClienteId(Long clienteId);
}
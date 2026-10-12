package com.transur.sigem.cliente.repository;

import com.transur.sigem.cliente.entity.ContactoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactoClienteRepository extends JpaRepository<ContactoCliente, Long> {
    List<ContactoCliente> findByClienteId(Long clienteId);
}
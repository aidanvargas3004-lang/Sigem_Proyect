package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(String nombre);

    boolean existsByNombreAndIdNot(String nombre, Long id);
}

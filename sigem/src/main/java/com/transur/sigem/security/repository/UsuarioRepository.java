package com.transur.sigem.security.repository;

import com.transur.sigem.security.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @EntityGraph(attributePaths = "rol")
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<Usuario> findByEstadoTrue();

    @Query("SELECT u FROM Usuario u JOIN FETCH u.rol r WHERE u.estado = true AND r.nombre IN :roles")
    List<Usuario> findActivosPorRoles(@Param("roles") java.util.Collection<String> roles);

    // ✅ ESTE ES EL MÉTODO QUE FALTA O NO SE RECONOCE
    @Query("SELECT u FROM Usuario u WHERE u.cliente.id = :clienteId")
    Optional<Usuario> findByClienteId(@Param("clienteId") Long clienteId);
}
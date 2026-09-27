package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    List<Usuario> findAllByOrderByUsernameAsc();

    List<Usuario> findByActivoTrueOrderByNombreCompletoAsc();

    List<Usuario> findByEmpleadoIdAndActivoTrue(Long empleadoId);

    long countByRolId(Long rolId);

    @Query("select distinct u from Usuario u join u.rol r join r.permisos p where u.activo = true and p = :permiso")
    List<Usuario> findActivosConPermiso(String permiso);

    @Transactional
    @Modifying
    @Query("update Usuario u set u.ultimoAcceso = :fecha where u.username = :username")
    void registrarAcceso(String username, LocalDateTime fecha);
}

package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.exception.NoEncontradoException;
import com.transmaqsur.sigem.model.Rol;
import com.transmaqsur.sigem.model.Usuario;
import com.transmaqsur.sigem.model.enums.Modulo;
import com.transmaqsur.sigem.repository.RolRepository;
import com.transmaqsur.sigem.repository.UsuarioRepository;
import com.transmaqsur.sigem.util.Entidades;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    public static final String ROL_ADMIN = "ADMINISTRADOR";
    private static final int MIN_PASSWORD = 6;

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    // ------------------------------------------------------------ Usuarios

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByUsernameAsc();
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarActivos() {
        return usuarioRepository.findByActivoTrueOrderByNombreCompletoAsc();
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Usuario", id));
    }

    public Usuario guardar(Usuario form, String passwordNueva) {
        if (form.isNuevo()) {
            if (usuarioRepository.existsByUsername(form.getUsername())) {
                throw new NegocioException("El nombre de usuario '" + form.getUsername() + "' ya existe");
            }
            validarPassword(passwordNueva);
            form.setPassword(passwordEncoder.encode(passwordNueva));
            return usuarioRepository.save(form);
        }
        Usuario u = obtener(form.getId());
        Entidades.copiar(form, u, "username", "password", "ultimoAcceso", "activo");
        if (passwordNueva != null && !passwordNueva.isBlank()) {
            validarPassword(passwordNueva);
            u.setPassword(passwordEncoder.encode(passwordNueva));
        }
        return u;
    }

    public void cambiarEstado(Long id, String usernameActual) {
        Usuario u = obtener(id);
        if (u.getUsername().equals(usernameActual)) {
            throw new NegocioException("No puede desactivar su propio usuario");
        }
        u.setActivo(!u.isActivo());
    }

    public void cambiarPasswordPropia(Long usuarioId, String actual, String nueva, String confirmacion) {
        Usuario u = obtener(usuarioId);
        if (!passwordEncoder.matches(actual, u.getPassword())) {
            throw new NegocioException("La contraseña actual no es correcta");
        }
        if (!nueva.equals(confirmacion)) {
            throw new NegocioException("La nueva contraseña y su confirmación no coinciden");
        }
        validarPassword(nueva);
        u.setPassword(passwordEncoder.encode(nueva));
    }

    private void validarPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD) {
            throw new NegocioException("La contraseña debe tener al menos " + MIN_PASSWORD + " caracteres");
        }
    }

    // ------------------------------------------------------------ Roles y permisos

    @Transactional(readOnly = true)
    public List<Rol> listarRoles() {
        return rolRepository.findAll().stream()
                .sorted((a, b) -> a.getNombre().compareTo(b.getNombre())).toList();
    }

    @Transactional(readOnly = true)
    public Rol obtenerRol(Long id) {
        return rolRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Rol", id));
    }

    @Transactional(readOnly = true)
    public long usuariosConRol(Long rolId) {
        return usuarioRepository.countByRolId(rolId);
    }

    public Rol guardarRol(Rol form) {
        Long id = form.isNuevo() ? 0L : form.getId();
        if (rolRepository.existsByNombreAndIdNot(form.getNombre(), id)) {
            throw new NegocioException("Ya existe un rol con el nombre " + form.getNombre());
        }
        Set<String> permisos = normalizarPermisos(form.getPermisos());
        Rol rol = form.isNuevo() ? form : obtenerRol(form.getId());
        if (!rol.isNuevo() && ROL_ADMIN.equals(rol.getNombre())) {
            // El administrador conserva siempre todos los permisos y su nombre, para evitar bloqueos
            form.setNombre(ROL_ADMIN);
            permisos = todosLosPermisos();
        }
        rol.setNombre(form.getNombre());
        rol.setDescripcion(form.getDescripcion());
        rol.getPermisos().clear();
        rol.getPermisos().addAll(permisos);
        return rolRepository.save(rol);
    }

    public void eliminarRol(Long id) {
        Rol rol = obtenerRol(id);
        if (ROL_ADMIN.equals(rol.getNombre())) {
            throw new NegocioException("El rol ADMINISTRADOR no se puede eliminar");
        }
        if (usuarioRepository.countByRolId(id) > 0) {
            throw new NegocioException("No se puede eliminar: hay usuarios con el rol " + rol.getNombre());
        }
        rolRepository.delete(rol);
    }

    /** Solo acepta permisos válidos y agrega VER cuando se otorga GESTIONAR. */
    public static Set<String> normalizarPermisos(Set<String> recibidos) {
        Set<String> resultado = new HashSet<>();
        if (recibidos == null) {
            return resultado;
        }
        for (Modulo m : Modulo.values()) {
            if (recibidos.contains(m.getPermisoGestionar())) {
                resultado.add(m.getPermisoGestionar());
                resultado.add(m.getPermisoVer());
            } else if (recibidos.contains(m.getPermisoVer())) {
                resultado.add(m.getPermisoVer());
            }
        }
        return resultado;
    }

    public static Set<String> todosLosPermisos() {
        Set<String> todos = new HashSet<>();
        for (Modulo m : Modulo.values()) {
            todos.add(m.getPermisoVer());
            todos.add(m.getPermisoGestionar());
        }
        return todos;
    }
}

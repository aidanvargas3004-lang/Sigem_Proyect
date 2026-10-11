package com.transmaqsur.sigem.dto;

import com.transmaqsur.sigem.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO del formulario de usuarios. Evita enlazar la entidad directamente a la vista:
 * la contraseña viaja en texto solo en este objeto y el servicio guarda su hash BCrypt.
 */
@Getter
@Setter
public class UsuarioForm {

    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(max = 120)
    private String nombreCompleto;

    @Email
    @Size(max = 120)
    private String email;

    @NotNull
    private Long rolId;

    private Long empleadoId;

    /** Obligatoria al crear; al editar, vacía significa "no cambiar". */
    private String password;

    public boolean isNuevo() {
        return id == null;
    }

    /** Mapea la entidad al DTO para editarla (nunca se expone el hash de la contraseña). */
    public static UsuarioForm desde(Usuario u) {
        UsuarioForm f = new UsuarioForm();
        f.setId(u.getId());
        f.setUsername(u.getUsername());
        f.setNombreCompleto(u.getNombreCompleto());
        f.setEmail(u.getEmail());
        f.setRolId(u.getRol().getId());
        f.setEmpleadoId(u.getEmpleado() != null ? u.getEmpleado().getId() : null);
        return f;
    }
}

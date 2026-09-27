package com.transmaqsur.sigem.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/** Usuario autenticado con datos extra para la interfaz. */
@Getter
public class UsuarioPrincipal extends User {

    private final Long usuarioId;
    private final String nombreCompleto;
    private final String rol;
    private final Long empleadoId;

    public UsuarioPrincipal(Long usuarioId, String username, String password, boolean activo,
                            String nombreCompleto, String rol, Long empleadoId,
                            Collection<? extends GrantedAuthority> authorities) {
        super(username, password, activo, true, true, true, authorities);
        this.usuarioId = usuarioId;
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.empleadoId = empleadoId;
    }

    public String getIniciales() {
        String[] partes = nombreCompleto.trim().split("\\s+");
        String ini = partes[0].substring(0, 1);
        if (partes.length > 1) {
            ini += partes[1].charAt(0);
        }
        return ini.toUpperCase();
    }
}

package com.transur.sigem.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** Acceso al nombre del usuario autenticado (para auditoría y movimientos). */
public final class UsuarioActual {

    private UsuarioActual() {
    }

    public static Optional<String> username() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return Optional.empty();
        }
        return Optional.of(auth.getName());
    }
}

package com.transmaqsur.sigem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** Completa automáticamente creadoPor / modificadoPor con el usuario en sesión. */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorActual")
public class JpaAuditoriaConfig {

    @Bean
    public AuditorAware<String> auditorActual() {
        return () -> Optional.of(usuarioActual());
    }

    public static String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "sistema";
        }
        return auth.getName();
    }
}

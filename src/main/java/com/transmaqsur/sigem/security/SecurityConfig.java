package com.transmaqsur.sigem.security;

import com.transmaqsur.sigem.model.enums.Modulo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Reglas de acceso: cada módulo exige el permiso MODULO_VER para consultar (GET)
 * y MODULO_GESTIONAR para registrar, editar o ejecutar acciones.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers("/css/**", "/js/**", "/img/**", "/webjars/**", "/favicon.svg", "/favicon.ico", "/login", "/error/**", "/error").permitAll();
            // La API GPS se protege con clave de dispositivo (X-API-KEY)
            auth.requestMatchers("/api/gps/**").permitAll();
            for (Modulo m : Modulo.values()) {
                reglasModulo(auth, m);
            }
            auth.anyRequest().authenticated();
        });
        http.formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll());
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?salir")
                .permitAll());
        http.exceptionHandling(ex -> ex.accessDeniedPage("/acceso-denegado"));
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
        return http.build();
    }

    private void reglasModulo(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth,
                              Modulo m) {
        for (String ruta : m.getRutas()) {
            auth.requestMatchers(HttpMethod.GET, ruta + "/nuevo", ruta + "/*/editar")
                    .hasAuthority(m.getPermisoGestionar());
            auth.requestMatchers(HttpMethod.GET, ruta, ruta + "/**").hasAuthority(m.getPermisoVer());
            auth.requestMatchers(ruta, ruta + "/**").hasAuthority(m.getPermisoGestionar());
        }
    }
}

package com.transur.sigem.security.config;

import com.transur.sigem.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.transur.sigem.security.util.JwtAuthFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF activo para las vistas (Thymeleaf agrega el token en cada th:action);
                // la API REST usa JWT sin estado, por eso se excluye.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/auth/**"))
                .headers(h -> h
                        .frameOptions(f -> f.sameOrigin())
                        .contentTypeOptions(c -> {})
                        .referrerPolicy(r -> r.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
                // Las vistas usan sesión; los endpoints REST siguen pudiendo trabajar con JWT.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/inicio-publico", "/catalogo").permitAll()
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/webjars/**", "/error", "/acceso-denegado").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        // API protegida por sesión o JWT y segmentada por área.
                        .requestMatchers("/api/maquinaria/**", "/api/servicios/**", "/api/asignaciones/**", "/api/incidencias/**", "/api/mantenimientos/**", "/api/gps/**").hasAnyRole("OPERACIONES", "ADMIN")
                        .requestMatchers("/api/activos/**").hasAnyRole("OPERACIONES", "LOGISTICA", "ADMIN")
                        .requestMatchers("/api/clientes/**").hasAnyRole("MARKETING", "VENTAS", "ADMIN")
                        .requestMatchers("/api/solicitudes/**", "/api/cotizaciones/**", "/api/contratos/**").hasAnyRole("CLIENTE", "VENTAS", "ADMIN")
                        .requestMatchers("/api/alquileres/**", "/api/ventas/**").hasAnyRole("VENTAS", "OPERACIONES", "ADMIN")
                        .requestMatchers("/api/marketing/**", "/api/marketing/**").hasAnyRole("MARKETING", "ADMIN")
                        .requestMatchers("/api/logistica/**", "/api/ordenes-compra/**", "/api/proveedores/**", "/api/recepciones/**").hasAnyRole("LOGISTICA", "ADMIN")
                        .requestMatchers("/api/empleados/**", "/api/documentos/**").hasAnyRole("RRHH", "ADMIN")
                        .requestMatchers("/api/sistema/auditoria/**").hasRole("ADMIN")
                        .requestMatchers("/api/sistema/**").authenticated()
                        .requestMatchers("/api/**").authenticated()
                        // Módulos por rol. ADMIN conserva acceso total.
                        .requestMatchers("/cliente/**").hasRole("CLIENTE")
                        .requestMatchers("/gerencia/**").hasAnyRole("GERENCIA", "ADMIN")
                        .requestMatchers("/ventas/**").hasAnyRole("VENTAS", "ADMIN")
                        .requestMatchers("/marketing/**").hasAnyRole("MARKETING", "ADMIN")
                        .requestMatchers("/rrhh/**").hasAnyRole("RRHH", "ADMIN")
                        .requestMatchers("/logistica/**").hasAnyRole("LOGISTICA", "ADMIN")
                        .requestMatchers("/operaciones/**", "/maquinaria/**", "/mantenimiento/**").hasAnyRole("OPERACIONES", "ADMIN")
                        .requestMatchers("/reportes/**").hasAnyRole("OPERACIONES", "GERENCIA", "ADMIN")
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").hasRole("ADMIN")
                        .requestMatchers("/seguridad/**").hasRole("ADMIN")
                        .requestMatchers("/dashboard").hasRole("ADMIN")
                        .requestMatchers("/inicio", "/notificaciones/**").authenticated()
                        // Todo lo no declarado exige sesión (antes era permitAll).
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/acceso-denegado")
                        // La API responde 401; las vistas redirigen al formulario de login.
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
                                response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                            } else {
                                response.sendRedirect(request.getContextPath() + "/login");
                            }
                        }))
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/inicio", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

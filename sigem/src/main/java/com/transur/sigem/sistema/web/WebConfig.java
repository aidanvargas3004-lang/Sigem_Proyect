package com.transur.sigem.sistema.web;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final NotificacionesInterceptor notificacionesInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(notificacionesInterceptor)
                .excludePathPatterns("/api/**", "/auth/**", "/css/**", "/js/**", "/images/**", "/webjars/**", "/error");
    }
}

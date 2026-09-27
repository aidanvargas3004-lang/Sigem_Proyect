package com.transmaqsur.sigem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * SIGEM - Sistema Integral de Gestión de Flotas y Maquinaria.
 * TRANSMAQ SUR S.A.C. - Arequipa, Perú.
 */
@EnableScheduling
@SpringBootApplication
public class SigemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SigemApplication.class, args);
    }
}

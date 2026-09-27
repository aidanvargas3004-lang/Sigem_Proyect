package com.transmaqsur.sigem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Ejecuta la revisión de alertas al arrancar y luego cada hora. */
@Component
@RequiredArgsConstructor
public class TareasProgramadas {

    private final AlertaService alertaService;

    @EventListener(ApplicationReadyEvent.class)
    public void alIniciar() {
        alertaService.generarAlertas();
    }

    @Scheduled(cron = "${sigem.alertas.cron:0 0 * * * *}")
    public void cadaHora() {
        alertaService.generarAlertas();
    }
}

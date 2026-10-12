package com.transur.sigem.operaciones.controller;

import com.transur.sigem.common.exception.RecursoNoEncontradoException;
import com.transur.sigem.operaciones.entity.Servicio;
import com.transur.sigem.operaciones.repository.AsignacionRepository;
import com.transur.sigem.operaciones.repository.IncidenciaRepository;
import com.transur.sigem.operaciones.repository.ServicioRepository;
import com.transur.sigem.operaciones.repository.UbicacionGpsRepository;
import com.transur.sigem.operaciones.service.ServicioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Ejecución de un servicio (RF010): registro de inicio y cierre con lecturas de la unidad. */
@Controller
@RequestMapping("/operaciones/servicios/{id}/ejecucion")
@RequiredArgsConstructor
public class EjecucionServicioViewController {

    private final ServicioRepository servicioRepository;
    private final AsignacionRepository asignacionRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final UbicacionGpsRepository ubicacionGpsRepository;
    private final ServicioService servicioService;

    @GetMapping
    public String ver(@PathVariable Long id, Model model) {
        Servicio s = servicioRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Servicio", id));
        model.addAttribute("s", s);
        model.addAttribute("asignaciones", asignacionRepository.findByServicioId(id));
        model.addAttribute("incidencias", incidenciaRepository.findByServicioId(id));
        model.addAttribute("ubicaciones", ubicacionGpsRepository.findByServicioIdOrderByFechaHoraDesc(id));
        model.addAttribute("ahora", LocalDateTime.now().withSecond(0).withNano(0));
        return "operaciones/ejecucion";
    }

    @PostMapping("/iniciar")
    public String iniciar(@PathVariable Long id, @RequestParam(required = false) BigDecimal horometro,
                          @RequestParam(required = false) BigDecimal km,
                          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora,
                          RedirectAttributes redirect) {
        try {
            servicioService.iniciar(id, horometro, km, fechaHora);
            redirect.addFlashAttribute("mensaje", "Inicio registrado: el servicio está EN CURSO.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/operaciones/servicios/" + id + "/ejecucion";
    }

    @PostMapping("/finalizar")
    public String finalizar(@PathVariable Long id, @RequestParam(required = false) BigDecimal horometro,
                            @RequestParam(required = false) BigDecimal km,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHora,
                            @RequestParam(required = false) String observaciones, RedirectAttributes redirect) {
        try {
            servicioService.finalizar(id, horometro, km, fechaHora, observaciones);
            redirect.addFlashAttribute("mensaje", "Cierre registrado: se calcularon las horas y kilómetros de uso.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/operaciones/servicios/" + id + "/ejecucion";
    }
}

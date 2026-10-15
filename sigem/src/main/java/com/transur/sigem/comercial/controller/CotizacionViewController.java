package com.transur.sigem.comercial.controller;

import com.transur.sigem.comercial.dto.CotizacionDetalleDTO;
import com.transur.sigem.comercial.dto.CotizacionRequest;
import com.transur.sigem.comercial.dto.CotizacionResponse;
import com.transur.sigem.comercial.service.CotizacionService;
import com.transur.sigem.comercial.service.SolicitudService;
import com.transur.sigem.maquinaria.repository.TarifaRepository;
import com.transur.sigem.maquinaria.service.MaquinariaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

@Controller
@RequestMapping("/ventas/cotizaciones")
@RequiredArgsConstructor
public class CotizacionViewController {
    private final CotizacionService cotizacionService;
    private final SolicitudService solicitudService;
    private final MaquinariaService maquinariaService;
    private final TarifaRepository tarifaRepository;

    @GetMapping
    public String listar(Model model) {
        var cotizaciones = cotizacionService.listar();
        LocalDate hoy = LocalDate.now();
        BigDecimal totalMes = cotizaciones.stream()
                .filter(c -> c.getFechaEmision() != null && c.getFechaEmision().getMonth() == hoy.getMonth() && c.getFechaEmision().getYear() == hoy.getYear())
                .map(c -> c.getTotal() == null ? BigDecimal.ZERO : c.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long aprobadas = cotizaciones.stream().filter(c -> "APROBADA".equals(c.getEstado())).count();
        double tasa = cotizaciones.isEmpty() ? 0 : aprobadas * 100.0 / cotizaciones.size();
        model.addAttribute("cotizaciones", cotizaciones);
        model.addAttribute("totalCotizadoMes", totalMes);
        model.addAttribute("tasaAceptacion", String.format("%.0f%%", tasa));
        return "ventas/cotizaciones";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        CotizacionRequest req = new CotizacionRequest();
        req.setFechaEmision(LocalDate.now());
        req.setVigencia(LocalDate.now().plusDays(15));
        req.getDetalles().add(new CotizacionDetalleDTO());
        prepararFormulario(model, req, "Nueva Cotización");
        return "ventas/cotizacion-form";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("cotizacion") CotizacionRequest req,
                          BindingResult result, Model model, RedirectAttributes redir) {
        if (result.hasErrors()) {
            prepararFormulario(model, req, "Nueva Cotización");
            return "ventas/cotizacion-form";
        }
        try {
            cotizacionService.crear(req);
            redir.addFlashAttribute("mensaje", "Cotización creada correctamente.");
            return "redirect:/ventas/cotizaciones";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            prepararFormulario(model, req, "Nueva Cotización");
            return "ventas/cotizacion-form";
        }
    }

    @PostMapping("/{id}/rechazar")
    public String rechazar(@PathVariable Long id, RedirectAttributes redir) {
        try {
            cotizacionService.cambiarEstado(id, "RECHAZADA");
            redir.addFlashAttribute("mensaje", "Cotización rechazada correctamente. El cliente verá el nuevo estado.");
        } catch (Exception e) {
            redir.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas/cotizaciones";
    }

    private void prepararFormulario(Model model, CotizacionRequest req, String titulo) {
        if (req.getDetalles() == null) req.setDetalles(new ArrayList<>());
        if (req.getDetalles().isEmpty()) req.getDetalles().add(new CotizacionDetalleDTO());
        model.addAttribute("cotizacion", req);
        model.addAttribute("solicitudes", solicitudService.listar().stream()
                .filter(s -> "PENDIENTE".equals(s.getEstado()))
                .toList());
        model.addAttribute("maquinarias", maquinariaService.listarActivos());
        model.addAttribute("todasLasTarifas", tarifaRepository.findAllWithMaquinaria());
        model.addAttribute("modo", "crear");
        model.addAttribute("titulo", titulo);
    }
}

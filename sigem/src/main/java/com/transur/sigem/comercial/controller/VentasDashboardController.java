package com.transur.sigem.comercial.controller;

import com.transur.sigem.comercial.entity.Solicitud;
import com.transur.sigem.comercial.service.CotizacionService;
import com.transur.sigem.comercial.service.ContratoService;
import com.transur.sigem.comercial.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentasDashboardController {
    private final SolicitudService solicitudService;
    private final CotizacionService cotizacionService;
    private final ContratoService contratoService;

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        List<Solicitud> solicitudes = solicitudService.listar();
        var cotizaciones = cotizacionService.listar();
        LocalDate hoy = LocalDate.now();

        long pendientes = solicitudes.stream().filter(s -> "PENDIENTE".equals(s.getEstado())).count();
        long enCotizacion = solicitudes.stream().filter(s -> "EN_COTIZACION".equals(s.getEstado())).count();
        long aprobadas = solicitudes.stream().filter(s -> "APROBADA".equals(s.getEstado())).count();
        long cotPendientes = cotizaciones.stream().filter(c -> "PENDIENTE".equals(c.getEstado()) || "ENVIADA".equals(c.getEstado())).count();
        BigDecimal cotizadoMes = cotizaciones.stream()
                .filter(c -> c.getFechaEmision() != null && c.getFechaEmision().getMonth() == hoy.getMonth() && c.getFechaEmision().getYear() == hoy.getYear())
                .map(c -> c.getTotal() == null ? BigDecimal.ZERO : c.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var contratos = contratoService.listar();
        long contratosVigentes = contratos.stream().filter(c -> "VIGENTE".equals(c.getEstado())).count();
        BigDecimal montoContratos = contratos.stream()
                .map(c -> c.getMontoTotal() == null ? BigDecimal.ZERO : c.getMontoTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalSolicitudes", solicitudes.size());
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("enCotizacion", enCotizacion);
        model.addAttribute("aprobadas", aprobadas);
        model.addAttribute("totalCotizaciones", cotizaciones.size());
        model.addAttribute("cotizacionesPendientes", cotPendientes);
        model.addAttribute("totalCotizadoMes", cotizadoMes);
        model.addAttribute("totalContratos", contratos.size());
        model.addAttribute("contratosVigentes", contratosVigentes);
        model.addAttribute("montoContratos", montoContratos);
        model.addAttribute("ultimasSolicitudes", solicitudes.stream().limit(8).toList());
        return "ventas/dashboard";
    }
}

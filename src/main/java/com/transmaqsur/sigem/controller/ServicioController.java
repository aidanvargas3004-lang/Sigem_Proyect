package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.ServicioTransporte;
import com.transmaqsur.sigem.model.enums.EstadoOperacion;
import com.transmaqsur.sigem.model.enums.Modalidad;
import com.transmaqsur.sigem.model.enums.Modulo;
import com.transmaqsur.sigem.security.Seguridad;
import com.transmaqsur.sigem.service.ClienteService;
import com.transmaqsur.sigem.service.ContratoService;
import com.transmaqsur.sigem.service.EmpleadoService;
import com.transmaqsur.sigem.service.ServicioTransporteService;
import com.transmaqsur.sigem.service.SolicitudService;
import com.transmaqsur.sigem.service.UnidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Servicios de transporte de carga (operaciones). */
@Controller
@RequestMapping("/servicios")
@RequiredArgsConstructor
public class ServicioController {

    private final ServicioTransporteService servicioService;
    private final ClienteService clienteService;
    private final ContratoService contratoService;
    private final SolicitudService solicitudService;
    private final UnidadService unidadService;
    private final EmpleadoService empleadoService;

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoOperacion estado, Model model) {
        model.addAttribute("servicios", servicioService.listar().stream().filter(s -> estado == null || s.getEstado() == estado).toList());
        model.addAttribute("estados", EstadoOperacion.values());
        model.addAttribute("estado", estado);
        return "operaciones/servicios";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Long contratoId, @RequestParam(required = false) Long solicitudId, Model model) {
        ServicioTransporte s = new ServicioTransporte();
        s.setModalidad(Modalidad.POR_KM);
        s.getAsignacion().setFechaInicio(LocalDate.now().plusDays(1).atTime(6, 0));
        s.getAsignacion().setFechaFin(LocalDate.now().plusDays(1).atTime(20, 0));
        if (contratoId != null) {
            s.setContrato(contratoService.obtener(contratoId));
            s.setCliente(s.getContrato().getCliente());
        }
        if (solicitudId != null) {
            var sol = solicitudService.obtener(solicitudId);
            s.setSolicitud(sol);
            s.setCliente(sol.getCliente());
            s.setOrigen(sol.getOrigen());
            s.setDestino(sol.getDestino());
        }
        model.addAttribute("servicio", s);
        return form(model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("servicio", servicioService.obtener(id));
        return form(model);
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("op", servicioService.obtener(id));
        model.addAttribute("ahora", LocalDateTime.now().withSecond(0).withNano(0));
        model.addAttribute("puedeGestionar", Seguridad.tienePermiso(Modulo.SERVICIOS.getPermisoGestionar()));
        return "operaciones/servicio-detalle";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("servicio") ServicioTransporte servicio, BindingResult br, Model model,
                          RedirectAttributes ra) {
        if (br.hasErrors()) {
            return form(model);
        }
        try {
            ServicioTransporte s = servicioService.guardar(servicio);
            ra.addFlashAttribute("exito", "Servicio " + s.getCodigo() + " programado. Unidad y conductor reservados.");
            return "redirect:/servicios/" + s.getId();
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return form(model);
        }
    }

    @PostMapping("/{id}/iniciar")
    public String iniciar(@PathVariable Long id, @RequestParam(required = false) BigDecimal lectura,
                          @RequestParam(required = false) LocalDateTime fechaHora, RedirectAttributes ra) {
        servicioService.iniciar(id, lectura, fechaHora);
        ra.addFlashAttribute("exito", "Servicio iniciado: la unidad está en ruta");
        return "redirect:/servicios/" + id;
    }

    @PostMapping("/{id}/finalizar")
    public String finalizar(@PathVariable Long id, @RequestParam(required = false) BigDecimal lectura,
                            @RequestParam(required = false) LocalDateTime fechaHora, RedirectAttributes ra) {
        servicioService.finalizar(id, lectura, fechaHora);
        ra.addFlashAttribute("exito", "Servicio finalizado y valorizado. Ya puede facturarse.");
        return "redirect:/servicios/" + id;
    }

    @PostMapping("/{id}/anular")
    public String anular(@PathVariable Long id, @RequestParam(required = false) String motivo, RedirectAttributes ra) {
        servicioService.anular(id, motivo);
        ra.addFlashAttribute("exito", "Servicio anulado: unidad y conductor liberados");
        return "redirect:/servicios/" + id;
    }

    private String form(Model model) {
        model.addAttribute("clientes", clienteService.listarActivos());
        model.addAttribute("contratos", contratoService.listarVigentes());
        model.addAttribute("solicitudes", solicitudService.listar());
        model.addAttribute("unidades", unidadService.listarVehiculos());
        model.addAttribute("conductores", empleadoService.listarPersonalDeCampo());
        model.addAttribute("modalidades", List.of(Modalidad.POR_KM, Modalidad.POR_VIAJE, Modalidad.POR_HORA));
        return "operaciones/servicio-form";
    }
}

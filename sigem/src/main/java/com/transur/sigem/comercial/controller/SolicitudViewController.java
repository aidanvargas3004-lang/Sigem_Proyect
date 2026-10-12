package com.transur.sigem.comercial.controller;

import com.transur.sigem.cliente.repository.ClienteRepository;
import com.transur.sigem.comercial.dto.SolicitudRequest;
import com.transur.sigem.comercial.entity.Solicitud;
import com.transur.sigem.comercial.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/ventas/solicitudes")
@RequiredArgsConstructor
public class SolicitudViewController {

    private final SolicitudService solicitudService;
    private final ClienteRepository clienteRepository;

    // LISTAR SOLICITUDES
    @GetMapping
    public String listar(Model model) {
        List<Solicitud> solicitudes = solicitudService.listar();

        // FORZAR CARGA DE CLIENTES PARA EVITAR LAZY INITIALIZATION EXCEPTION EN THYMELEAF
        solicitudes.forEach(s -> {
            if (s.getCliente() != null) {
                s.getCliente().getRazonSocial(); // Esto dispara el SELECT del cliente
            }
        });

        long total = solicitudes.size();
        long pendientes = solicitudes.stream().filter(s -> "PENDIENTE".equals(s.getEstado())).count();
        long enCotizacion = solicitudes.stream().filter(s -> "EN_COTIZACION".equals(s.getEstado())).count();
        long aprobadas = solicitudes.stream().filter(s -> "APROBADA".equals(s.getEstado())).count();

        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("totalSolicitudes", total);
        model.addAttribute("pendientes", pendientes);
        model.addAttribute("enCotizacion", enCotizacion);
        model.addAttribute("aprobadas", aprobadas);

        return "ventas/solicitudes";
    }

    // FORMULARIO NUEVA SOLICITUD
    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("solicitud", new SolicitudRequest());
        model.addAttribute("clientes", clienteRepository.findAll());
        model.addAttribute("modo", "crear");
        model.addAttribute("titulo", "Nueva Solicitud");
        return "ventas/solicitud-form";
    }

    // FORMULARIO EDITAR SOLICITUD
    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model) {
        Solicitud sol = solicitudService.obtenerPorId(id);

        // Mapear entidad a DTO para el formulario
        SolicitudRequest dto = new SolicitudRequest();
        dto.setClienteId(sol.getCliente().getId());
        dto.setTipoSolicitud(sol.getTipoSolicitud());
        dto.setDescripcion(sol.getDescripcion());

        model.addAttribute("solicitud", dto);
        model.addAttribute("clientes", clienteRepository.findAll());
        model.addAttribute("modo", "editar");
        model.addAttribute("solicitudId", id);
        model.addAttribute("titulo", "Editar Solicitud #" + id);
        return "ventas/solicitud-form";
    }

    // GUARDAR (CREAR O ACTUALIZAR)
    @PostMapping
    public String guardar(
            @Valid @ModelAttribute SolicitudRequest req,
            @RequestParam(required = false) Long solicitudId,
            RedirectAttributes redirect) {

        try {
            if (solicitudId != null) {
                solicitudService.actualizar(solicitudId, req);
                redirect.addFlashAttribute("mensaje", "✅ Solicitud actualizada correctamente");
            } else {
                solicitudService.crear(req);
                redirect.addFlashAttribute("mensaje", "✅ Solicitud creada correctamente");
            }
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "❌ Error: " + e.getMessage());
            return "redirect:/ventas/solicitudes/nuevo";
        }

        return "redirect:/ventas/solicitudes";
    }
}
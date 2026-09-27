package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Cotizacion;
import com.transmaqsur.sigem.model.Solicitud;
import com.transmaqsur.sigem.model.enums.EstadoSolicitud;
import com.transmaqsur.sigem.model.enums.Modalidad;
import com.transmaqsur.sigem.model.enums.TipoServicio;
import com.transmaqsur.sigem.model.enums.TipoUnidad;
import com.transmaqsur.sigem.repository.ContactoRepository;
import com.transmaqsur.sigem.repository.ContratoRepository;
import com.transmaqsur.sigem.service.ClienteService;
import com.transmaqsur.sigem.service.CotizacionService;
import com.transmaqsur.sigem.service.SolicitudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/** Solicitudes de clientes y cotizaciones. */
@Controller
@RequiredArgsConstructor
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final CotizacionService cotizacionService;
    private final ClienteService clienteService;
    private final ContactoRepository contactoRepository;
    private final ContratoRepository contratoRepository;

    // ------------------------------------------------------------ Solicitudes

    @GetMapping("/solicitudes")
    public String solicitudes(Model model) {
        model.addAttribute("solicitudes", solicitudService.listar());
        return "comercial/solicitudes";
    }

    @GetMapping("/solicitudes/nuevo")
    public String nuevaSolicitud(@RequestParam(required = false) Long clienteId, Model model) {
        Solicitud s = new Solicitud();
        if (clienteId != null) {
            s.setCliente(clienteService.obtener(clienteId));
        }
        model.addAttribute("solicitud", s);
        return formSolicitud(model);
    }

    @GetMapping("/solicitudes/{id}/editar")
    public String editarSolicitud(@PathVariable Long id, Model model) {
        model.addAttribute("solicitud", solicitudService.obtener(id));
        return formSolicitud(model);
    }

    @GetMapping("/solicitudes/{id}")
    public String detalleSolicitud(@PathVariable Long id, Model model) {
        model.addAttribute("solicitud", solicitudService.obtener(id));
        model.addAttribute("cotizaciones", cotizacionService.deSolicitud(id));
        return "comercial/solicitud-detalle";
    }

    @PostMapping("/solicitudes/guardar")
    public String guardarSolicitud(@Valid @ModelAttribute("solicitud") Solicitud solicitud, BindingResult br, Model model,
                                   RedirectAttributes ra) {
        if (br.hasErrors()) {
            return formSolicitud(model);
        }
        try {
            Solicitud s = solicitudService.guardar(solicitud);
            ra.addFlashAttribute("exito", "Solicitud " + s.getCodigo() + " registrada");
            return "redirect:/solicitudes/" + s.getId();
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return formSolicitud(model);
        }
    }

    @PostMapping("/solicitudes/{id}/estado")
    public String estadoSolicitud(@PathVariable Long id, @RequestParam EstadoSolicitud estado, RedirectAttributes ra) {
        solicitudService.cambiarEstado(id, estado);
        ra.addFlashAttribute("exito", "Solicitud marcada como " + estado.getLabel().toLowerCase());
        return "redirect:/solicitudes/" + id;
    }

    private String formSolicitud(Model model) {
        model.addAttribute("clientes", clienteService.listarActivos());
        model.addAttribute("contactos", contactoRepository.findAll());
        model.addAttribute("tiposServicio", TipoServicio.values());
        model.addAttribute("tiposUnidad", TipoUnidad.values());
        return "comercial/solicitud-form";
    }

    // ------------------------------------------------------------ Cotizaciones

    @GetMapping("/cotizaciones")
    public String cotizaciones(Model model) {
        model.addAttribute("cotizaciones", cotizacionService.listar());
        return "comercial/cotizaciones";
    }

    @GetMapping("/cotizaciones/nuevo")
    public String nuevaCotizacion(@RequestParam(required = false) Long solicitudId, Model model) {
        Cotizacion c = new Cotizacion();
        if (solicitudId != null) {
            Solicitud s = solicitudService.obtener(solicitudId);
            c.setSolicitud(s);
            c.setCliente(s.getCliente());
        }
        c.setCondiciones("Precios en soles, no incluyen IGV. Incluye operador y combustible salvo indicación contraria. "
                + "Forma de pago: 30 días de emitida la factura.");
        model.addAttribute("cotizacion", c);
        return formCotizacion(model);
    }

    @GetMapping("/cotizaciones/{id}/editar")
    public String editarCotizacion(@PathVariable Long id, Model model) {
        model.addAttribute("cotizacion", cotizacionService.obtener(id));
        return formCotizacion(model);
    }

    @GetMapping("/cotizaciones/{id}")
    public String detalleCotizacion(@PathVariable Long id, Model model) {
        model.addAttribute("cotizacion", cotizacionService.obtener(id));
        model.addAttribute("contrato", contratoRepository.findFirstByCotizacionId(id).orElse(null));
        model.addAttribute("tiposUnidad", TipoUnidad.values());
        model.addAttribute("modalidades", Modalidad.values());
        return "comercial/cotizacion-detalle";
    }

    @GetMapping("/cotizaciones/{id}/imprimir")
    public String imprimirCotizacion(@PathVariable Long id, Model model) {
        model.addAttribute("cotizacion", cotizacionService.obtener(id));
        return "comercial/cotizacion-imprimir";
    }

    @PostMapping("/cotizaciones/guardar")
    public String guardarCotizacion(@Valid @ModelAttribute("cotizacion") Cotizacion cotizacion, BindingResult br, Model model,
                                    RedirectAttributes ra) {
        if (br.hasErrors()) {
            return formCotizacion(model);
        }
        try {
            Cotizacion c = cotizacionService.guardar(cotizacion);
            ra.addFlashAttribute("exito", "Cotización " + c.getCodigo() + " guardada");
            return "redirect:/cotizaciones/" + c.getId();
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return formCotizacion(model);
        }
    }

    @PostMapping("/cotizaciones/{id}/detalles")
    public String agregarDetalle(@PathVariable Long id, @RequestParam String descripcion,
                                 @RequestParam(required = false) TipoUnidad tipoUnidad, @RequestParam Modalidad modalidad,
                                 @RequestParam BigDecimal cantidad, @RequestParam BigDecimal precio, RedirectAttributes ra) {
        cotizacionService.agregarDetalle(id, descripcion, tipoUnidad, modalidad, cantidad, precio);
        ra.addFlashAttribute("exito", "Línea agregada");
        return "redirect:/cotizaciones/" + id;
    }

    @PostMapping("/cotizaciones/{id}/detalles/{detalleId}/eliminar")
    public String quitarDetalle(@PathVariable Long id, @PathVariable Long detalleId) {
        cotizacionService.quitarDetalle(id, detalleId);
        return "redirect:/cotizaciones/" + id;
    }

    @PostMapping("/cotizaciones/{id}/enviar")
    public String enviar(@PathVariable Long id, RedirectAttributes ra) {
        cotizacionService.enviar(id);
        ra.addFlashAttribute("exito", "Cotización enviada al cliente");
        return "redirect:/cotizaciones/" + id;
    }

    @PostMapping("/cotizaciones/{id}/aceptar")
    public String aceptar(@PathVariable Long id, RedirectAttributes ra) {
        cotizacionService.aceptar(id);
        ra.addFlashAttribute("exito", "Cotización aceptada. Ya puede generar el contrato.");
        return "redirect:/cotizaciones/" + id;
    }

    @PostMapping("/cotizaciones/{id}/rechazar")
    public String rechazar(@PathVariable Long id, RedirectAttributes ra) {
        cotizacionService.rechazar(id);
        ra.addFlashAttribute("exito", "Cotización marcada como rechazada");
        return "redirect:/cotizaciones/" + id;
    }

    private String formCotizacion(Model model) {
        model.addAttribute("clientes", clienteService.listarActivos());
        model.addAttribute("solicitudes", solicitudService.listarPendientes());
        return "comercial/cotizacion-form";
    }
}

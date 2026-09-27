package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Cliente;
import com.transmaqsur.sigem.model.Contacto;
import com.transmaqsur.sigem.model.enums.TipoCliente;
import com.transmaqsur.sigem.model.enums.TipoDocumento;
import com.transmaqsur.sigem.repository.AlquilerRepository;
import com.transmaqsur.sigem.repository.ContratoRepository;
import com.transmaqsur.sigem.repository.CotizacionRepository;
import com.transmaqsur.sigem.repository.OportunidadRepository;
import com.transmaqsur.sigem.repository.ServicioTransporteRepository;
import com.transmaqsur.sigem.repository.SolicitudRepository;
import com.transmaqsur.sigem.repository.VentaRepository;
import com.transmaqsur.sigem.service.ClienteService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final SolicitudRepository solicitudRepository;
    private final CotizacionRepository cotizacionRepository;
    private final ContratoRepository contratoRepository;
    private final VentaRepository ventaRepository;
    private final OportunidadRepository oportunidadRepository;
    private final AlquilerRepository alquilerRepository;
    private final ServicioTransporteRepository servicioRepository;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteService.listar());
        return "clientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return form(model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.obtener(id));
        return form(model);
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("cliente", clienteService.obtener(id));
        model.addAttribute("contacto", new Contacto());
        model.addAttribute("solicitudes", solicitudRepository.findByClienteIdOrderByIdDesc(id));
        model.addAttribute("cotizaciones", cotizacionRepository.findByClienteIdOrderByIdDesc(id));
        model.addAttribute("contratos", contratoRepository.findByClienteIdOrderByIdDesc(id));
        model.addAttribute("ventas", ventaRepository.findByClienteIdOrderByIdDesc(id));
        model.addAttribute("oportunidades", oportunidadRepository.findByClienteIdOrderByFechaCreacionDesc(id));
        model.addAttribute("alquileres", alquilerRepository.findAllByOrderByIdDesc().stream().filter(a -> a.getCliente().getId().equals(id)).toList());
        model.addAttribute("servicios", servicioRepository.findAllByOrderByIdDesc().stream().filter(s -> s.getCliente().getId().equals(id)).toList());
        return "clientes/detalle";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cliente") Cliente cliente, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return form(model);
        }
        try {
            Cliente c = clienteService.guardar(cliente);
            ra.addFlashAttribute("exito", "Cliente " + c.getRazonSocial() + " guardado");
            return "redirect:/clientes/" + c.getId();
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return form(model);
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, RedirectAttributes ra) {
        clienteService.cambiarEstado(id);
        ra.addFlashAttribute("exito", "Estado del cliente actualizado");
        return "redirect:/clientes/" + id;
    }

    @PostMapping("/{id}/contactos")
    public String agregarContacto(@PathVariable Long id, @Valid @ModelAttribute("contacto") Contacto contacto, BindingResult br,
                                  RedirectAttributes ra) {
        if (br.hasErrors()) {
            ra.addFlashAttribute("error", "Revise los datos del contacto: el nombre es obligatorio y el correo debe ser válido");
        } else {
            clienteService.agregarContacto(id, contacto);
            ra.addFlashAttribute("exito", "Contacto agregado");
        }
        return "redirect:/clientes/" + id + "#contactos";
    }

    @PostMapping("/{id}/contactos/{contactoId}/principal")
    public String principal(@PathVariable Long id, @PathVariable Long contactoId) {
        clienteService.marcarPrincipal(id, contactoId);
        return "redirect:/clientes/" + id + "#contactos";
    }

    @PostMapping("/{id}/contactos/{contactoId}/eliminar")
    public String eliminarContacto(@PathVariable Long id, @PathVariable Long contactoId, RedirectAttributes ra) {
        clienteService.eliminarContacto(id, contactoId);
        ra.addFlashAttribute("exito", "Contacto eliminado");
        return "redirect:/clientes/" + id + "#contactos";
    }

    private String form(Model model) {
        model.addAttribute("tiposDocumento", TipoDocumento.values());
        model.addAttribute("tiposCliente", TipoCliente.values());
        return "clientes/form";
    }
}

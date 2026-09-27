package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Venta;
import com.transmaqsur.sigem.model.enums.TipoComprobante;
import com.transmaqsur.sigem.service.ClienteService;
import com.transmaqsur.sigem.service.InventarioService;
import com.transmaqsur.sigem.service.VentaService;
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

/** Ventas y facturación. */
@Controller
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;
    private final ClienteService clienteService;
    private final InventarioService inventarioService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("ventas", ventaService.listar());
        return "ventas/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Long clienteId, Model model) {
        Venta v = new Venta();
        if (clienteId != null) {
            v.setCliente(clienteService.obtener(clienteId));
        }
        model.addAttribute("venta", v);
        return form(model);
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("venta", ventaService.obtener(id));
        return form(model);
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Venta v = ventaService.obtener(id);
        model.addAttribute("venta", v);
        if (v.isEditable()) {
            model.addAttribute("pendientes", ventaService.pendientesDeFacturar(v.getCliente().getId()));
            model.addAttribute("stock", inventarioService.conStock());
        }
        return "ventas/detalle";
    }

    @GetMapping("/{id}/imprimir")
    public String imprimir(@PathVariable Long id, Model model) {
        model.addAttribute("venta", ventaService.obtener(id));
        return "ventas/imprimir";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("venta") Venta venta, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return form(model);
        }
        try {
            Venta v = ventaService.guardar(venta);
            ra.addFlashAttribute("exito", "Borrador de comprobante creado. Agregue las líneas a facturar.");
            return "redirect:/ventas/" + v.getId();
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return form(model);
        }
    }

    /** Atajo desde un alquiler (A) o servicio (S) finalizado. */
    @PostMapping("/facturar/{tipo}/{operacionId}")
    public String facturar(@PathVariable String tipo, @PathVariable Long operacionId, RedirectAttributes ra) {
        Venta v = ventaService.facturarOperacion(tipo, operacionId);
        ra.addFlashAttribute("exito", "Borrador creado con la operación. Revise y emita el comprobante.");
        return "redirect:/ventas/" + v.getId();
    }

    @PostMapping("/{id}/operaciones")
    public String agregarOperacion(@PathVariable Long id, @RequestParam String referencia, RedirectAttributes ra) {
        ventaService.agregarOperacion(id, referencia);
        ra.addFlashAttribute("exito", "Operación agregada al comprobante");
        return "redirect:/ventas/" + id;
    }

    @PostMapping("/{id}/repuestos")
    public String agregarRepuesto(@PathVariable Long id, @RequestParam String inventario, @RequestParam BigDecimal cantidad,
                                  @RequestParam(required = false) BigDecimal precio, RedirectAttributes ra) {
        String[] partes = inventario.split("-");
        if (partes.length != 2) {
            throw new NegocioException("Seleccione el repuesto y almacén");
        }
        ventaService.agregarRepuesto(id, Long.valueOf(partes[0]), Long.valueOf(partes[1]), cantidad, precio);
        ra.addFlashAttribute("exito", "Repuesto agregado");
        return "redirect:/ventas/" + id;
    }

    @PostMapping("/{id}/conceptos")
    public String agregarConcepto(@PathVariable Long id, @RequestParam String descripcion, @RequestParam BigDecimal cantidad,
                                  @RequestParam BigDecimal precio, RedirectAttributes ra) {
        ventaService.agregarConcepto(id, descripcion, cantidad, precio);
        ra.addFlashAttribute("exito", "Concepto agregado");
        return "redirect:/ventas/" + id;
    }

    @PostMapping("/{id}/detalles/{detalleId}/eliminar")
    public String quitarLinea(@PathVariable Long id, @PathVariable Long detalleId) {
        ventaService.quitarLinea(id, detalleId);
        return "redirect:/ventas/" + id;
    }

    @PostMapping("/{id}/emitir")
    public String emitir(@PathVariable Long id, RedirectAttributes ra) {
        ventaService.emitir(id);
        ra.addFlashAttribute("exito", "Comprobante emitido");
        return "redirect:/ventas/" + id;
    }

    @PostMapping("/{id}/pago")
    public String pago(@PathVariable Long id, @RequestParam String metodoPago, @RequestParam(required = false) LocalDate fechaPago,
                       RedirectAttributes ra) {
        ventaService.registrarPago(id, metodoPago, fechaPago);
        ra.addFlashAttribute("exito", "Pago registrado");
        return "redirect:/ventas/" + id;
    }

    @PostMapping("/{id}/anular")
    public String anular(@PathVariable Long id, RedirectAttributes ra) {
        boolean borrador = ventaService.obtener(id).isEditable();
        ventaService.anular(id);
        ra.addFlashAttribute("exito", borrador ? "Borrador eliminado" : "Comprobante anulado: se revirtieron stock y operaciones");
        return borrador ? "redirect:/ventas" : "redirect:/ventas/" + id;
    }

    private String form(Model model) {
        model.addAttribute("clientes", clienteService.listarActivos());
        model.addAttribute("tipos", TipoComprobante.values());
        return "ventas/form";
    }
}

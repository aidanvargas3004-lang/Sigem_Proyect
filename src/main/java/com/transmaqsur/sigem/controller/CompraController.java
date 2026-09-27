package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Compra;
import com.transmaqsur.sigem.model.Proveedor;
import com.transmaqsur.sigem.repository.CompraRepository;
import com.transmaqsur.sigem.service.CompraService;
import com.transmaqsur.sigem.service.InventarioService;
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

/** Proveedores y órdenes de compra. */
@Controller
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;
    private final InventarioService inventarioService;
    private final CompraRepository compraRepository;

    // ------------------------------------------------------------ Proveedores

    @GetMapping("/proveedores")
    public String proveedores(Model model) {
        model.addAttribute("proveedores", compraService.listarProveedores());
        return "compras/proveedores";
    }

    @GetMapping("/proveedores/nuevo")
    public String nuevoProveedor(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "compras/proveedor-form";
    }

    @GetMapping("/proveedores/{id}/editar")
    public String editarProveedor(@PathVariable Long id, Model model) {
        model.addAttribute("proveedor", compraService.obtenerProveedor(id));
        return "compras/proveedor-form";
    }

    @GetMapping("/proveedores/{id}")
    public String detalleProveedor(@PathVariable Long id, Model model) {
        model.addAttribute("proveedor", compraService.obtenerProveedor(id));
        model.addAttribute("compras", compraRepository.findByProveedorIdOrderByIdDesc(id));
        return "compras/proveedor-detalle";
    }

    @PostMapping("/proveedores/guardar")
    public String guardarProveedor(@Valid @ModelAttribute("proveedor") Proveedor proveedor, BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return "compras/proveedor-form";
        }
        try {
            Proveedor p = compraService.guardarProveedor(proveedor);
            ra.addFlashAttribute("exito", "Proveedor guardado");
            return "redirect:/proveedores/" + p.getId();
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return "compras/proveedor-form";
        }
    }

    // ------------------------------------------------------------ Órdenes de compra

    @GetMapping("/compras")
    public String compras(Model model) {
        model.addAttribute("compras", compraService.listar());
        return "compras/lista";
    }

    @GetMapping("/compras/nuevo")
    public String nuevaCompra(Model model) {
        model.addAttribute("compra", new Compra());
        return formCompra(model);
    }

    @GetMapping("/compras/{id}/editar")
    public String editarCompra(@PathVariable Long id, Model model) {
        model.addAttribute("compra", compraService.obtener(id));
        return formCompra(model);
    }

    @GetMapping("/compras/{id}")
    public String detalleCompra(@PathVariable Long id, Model model) {
        model.addAttribute("compra", compraService.obtener(id));
        model.addAttribute("repuestos", inventarioService.listarRepuestosActivos());
        return "compras/detalle";
    }

    @PostMapping("/compras/guardar")
    public String guardarCompra(@Valid @ModelAttribute("compra") Compra compra, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return formCompra(model);
        }
        try {
            Compra c = compraService.guardar(compra);
            ra.addFlashAttribute("exito", "Orden " + c.getCodigo() + " guardada. Agregue los repuestos a comprar.");
            return "redirect:/compras/" + c.getId();
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return formCompra(model);
        }
    }

    @PostMapping("/compras/{id}/detalles")
    public String agregarDetalle(@PathVariable Long id, @RequestParam Long repuestoId, @RequestParam BigDecimal cantidad,
                                 @RequestParam BigDecimal precio, RedirectAttributes ra) {
        compraService.agregarDetalle(id, repuestoId, cantidad, precio);
        ra.addFlashAttribute("exito", "Repuesto agregado a la orden");
        return "redirect:/compras/" + id;
    }

    @PostMapping("/compras/{id}/detalles/{detalleId}/eliminar")
    public String quitarDetalle(@PathVariable Long id, @PathVariable Long detalleId) {
        compraService.quitarDetalle(id, detalleId);
        return "redirect:/compras/" + id;
    }

    @PostMapping("/compras/{id}/aprobar")
    public String aprobar(@PathVariable Long id, RedirectAttributes ra) {
        compraService.aprobar(id);
        ra.addFlashAttribute("exito", "Orden aprobada");
        return "redirect:/compras/" + id;
    }

    @PostMapping("/compras/{id}/recibir")
    public String recibir(@PathVariable Long id, @RequestParam(required = false) String comprobante, RedirectAttributes ra) {
        compraService.recibir(id, comprobante);
        ra.addFlashAttribute("exito", "Mercadería recibida: el stock del almacén fue actualizado");
        return "redirect:/compras/" + id;
    }

    @PostMapping("/compras/{id}/anular")
    public String anular(@PathVariable Long id, RedirectAttributes ra) {
        compraService.anular(id);
        ra.addFlashAttribute("exito", "Orden anulada");
        return "redirect:/compras/" + id;
    }

    private String formCompra(Model model) {
        model.addAttribute("proveedores", compraService.listarProveedoresActivos());
        model.addAttribute("almacenes", inventarioService.listarAlmacenesActivos());
        return "compras/form";
    }
}

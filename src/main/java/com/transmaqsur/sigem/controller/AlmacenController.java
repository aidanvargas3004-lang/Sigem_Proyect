package com.transmaqsur.sigem.controller;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Almacen;
import com.transmaqsur.sigem.model.Inventario;
import com.transmaqsur.sigem.model.Repuesto;
import com.transmaqsur.sigem.service.EmpleadoService;
import com.transmaqsur.sigem.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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
import java.util.List;

/** Almacenes, catálogo de repuestos, stock e historial de movimientos (kardex). */
@Controller
@RequiredArgsConstructor
public class AlmacenController {

    private final InventarioService inventarioService;
    private final EmpleadoService empleadoService;

    // ------------------------------------------------------------ Almacenes

    @GetMapping("/almacenes")
    public String almacenes(Model model) {
        model.addAttribute("almacenes", inventarioService.listarAlmacenes());
        return "almacen/almacenes";
    }

    @GetMapping("/almacenes/nuevo")
    public String nuevoAlmacen(Model model) {
        model.addAttribute("almacen", new Almacen());
        model.addAttribute("empleados", empleadoService.listarActivos());
        return "almacen/almacen-form";
    }

    @GetMapping("/almacenes/{id}/editar")
    public String editarAlmacen(@PathVariable Long id, Model model) {
        model.addAttribute("almacen", inventarioService.obtenerAlmacen(id));
        model.addAttribute("empleados", empleadoService.listarActivos());
        return "almacen/almacen-form";
    }

    @GetMapping("/almacenes/{id}")
    public String detalleAlmacen(@PathVariable Long id, Model model) {
        model.addAttribute("almacen", inventarioService.obtenerAlmacen(id));
        List<Inventario> stock = inventarioService.inventarioPorAlmacen(id);
        model.addAttribute("stock", stock);
        model.addAttribute("valorizado", stock.stream().map(i -> i.getCantidad().multiply(i.getRepuesto().getPrecioUnitario()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return "almacen/almacen-detalle";
    }

    @PostMapping("/almacenes/guardar")
    public String guardarAlmacen(@Valid @ModelAttribute("almacen") Almacen almacen, BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("empleados", empleadoService.listarActivos());
            return "almacen/almacen-form";
        }
        Almacen a = inventarioService.guardarAlmacen(almacen);
        ra.addFlashAttribute("exito", "Almacén guardado");
        return "redirect:/almacenes/" + a.getId();
    }

    // ------------------------------------------------------------ Repuestos

    @GetMapping("/repuestos")
    public String repuestos(Model model) {
        List<Repuesto> repuestos = inventarioService.listarRepuestos();
        model.addAttribute("repuestos", repuestos);
        model.addAttribute("stock", repuestos.stream().collect(java.util.stream.Collectors.toMap(Repuesto::getId,
                r -> inventarioService.stockTotal(r.getId()))));
        return "almacen/repuestos";
    }

    @GetMapping("/repuestos/nuevo")
    public String nuevoRepuesto(Model model) {
        model.addAttribute("repuesto", new Repuesto());
        return "almacen/repuesto-form";
    }

    @GetMapping("/repuestos/{id}/editar")
    public String editarRepuesto(@PathVariable Long id, Model model) {
        model.addAttribute("repuesto", inventarioService.obtenerRepuesto(id));
        return "almacen/repuesto-form";
    }

    @GetMapping("/repuestos/{id}")
    public String detalleRepuesto(@PathVariable Long id, Model model) {
        model.addAttribute("repuesto", inventarioService.obtenerRepuesto(id));
        model.addAttribute("stock", inventarioService.inventarioPorRepuesto(id));
        model.addAttribute("stockTotal", inventarioService.stockTotal(id));
        model.addAttribute("movimientos", inventarioService.movimientos(null, id, PageRequest.of(0, 30)).getContent());
        return "almacen/repuesto-detalle";
    }

    @PostMapping("/repuestos/guardar")
    public String guardarRepuesto(@Valid @ModelAttribute("repuesto") Repuesto repuesto, BindingResult br, RedirectAttributes ra) {
        if (br.hasErrors()) {
            return "almacen/repuesto-form";
        }
        try {
            Repuesto r = inventarioService.guardarRepuesto(repuesto);
            ra.addFlashAttribute("exito", "Repuesto " + r.getCodigo() + " guardado");
            return "redirect:/repuestos/" + r.getId();
        } catch (NegocioException e) {
            br.reject("negocio", e.getMessage());
            return "almacen/repuesto-form";
        }
    }

    // ------------------------------------------------------------ Inventario y kardex

    @GetMapping("/inventario")
    public String inventario(@RequestParam(required = false) Long almacenId, Model model) {
        List<Inventario> stock = almacenId == null ? inventarioService.inventarioCompleto() : inventarioService.inventarioPorAlmacen(almacenId);
        model.addAttribute("stock", stock);
        model.addAttribute("bajoMinimo", inventarioService.bajoMinimo());
        model.addAttribute("almacenes", inventarioService.listarAlmacenesActivos());
        model.addAttribute("repuestos", inventarioService.listarRepuestosActivos());
        model.addAttribute("almacenId", almacenId);
        model.addAttribute("valorizado", stock.stream().map(i -> i.getCantidad().multiply(i.getRepuesto().getPrecioUnitario()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        return "almacen/inventario";
    }

    @GetMapping("/inventario/movimientos")
    public String movimientos(@RequestParam(required = false) Long almacenId, @RequestParam(required = false) Long repuestoId,
                              @RequestParam(defaultValue = "0") int pagina, Model model) {
        model.addAttribute("pagina", inventarioService.movimientos(almacenId, repuestoId, PageRequest.of(pagina, 25)));
        model.addAttribute("almacenes", inventarioService.listarAlmacenes());
        model.addAttribute("repuestos", inventarioService.listarRepuestos());
        model.addAttribute("almacenId", almacenId);
        model.addAttribute("repuestoId", repuestoId);
        return "almacen/movimientos";
    }

    @PostMapping("/inventario/ajuste")
    public String ajustar(@RequestParam Long almacenId, @RequestParam Long repuestoId, @RequestParam BigDecimal cantidadReal,
                          @RequestParam String motivo, RedirectAttributes ra) {
        inventarioService.ajustar(almacenId, repuestoId, cantidadReal, motivo);
        ra.addFlashAttribute("exito", "Ajuste de inventario registrado");
        return "redirect:/inventario";
    }

    @PostMapping("/inventario/transferencia")
    public String transferir(@RequestParam Long origenId, @RequestParam Long destinoId, @RequestParam Long repuestoId,
                             @RequestParam BigDecimal cantidad, RedirectAttributes ra) {
        inventarioService.transferir(origenId, destinoId, repuestoId, cantidad);
        ra.addFlashAttribute("exito", "Transferencia registrada");
        return "redirect:/inventario";
    }
}

package com.transur.sigem.logistica.controller;

import com.transur.sigem.compras.dto.OrdenCompraDetalleDTO;
import com.transur.sigem.compras.dto.OrdenCompraRequest;
import com.transur.sigem.compras.dto.RecepcionDetalleDTO;
import com.transur.sigem.compras.dto.RecepcionRequest;
import com.transur.sigem.compras.entity.OrdenCompraDetalle;
import com.transur.sigem.compras.service.OrdenCompraService;
import com.transur.sigem.compras.service.ProveedorService;
import com.transur.sigem.compras.service.RecepcionService;
import com.transur.sigem.compras.dto.ProveedorRequest;
import com.transur.sigem.logistica.dto.AlmacenRequest;
import com.transur.sigem.logistica.dto.InventarioMaquinariaRequest;
import com.transur.sigem.logistica.dto.MovimientoRequest;
import com.transur.sigem.logistica.dto.RepuestoRequest;
import com.transur.sigem.logistica.dto.UbicacionRequest;
import com.transur.sigem.logistica.entity.CategoriaRepuesto;
import com.transur.sigem.logistica.entity.Repuesto;
import com.transur.sigem.logistica.repository.CategoriaRepuestoRepository;
import com.transur.sigem.logistica.repository.UbicacionAlmacenRepository;
import com.transur.sigem.logistica.service.AlmacenService;
import com.transur.sigem.logistica.service.InventarioMaquinariaService;
import com.transur.sigem.logistica.service.MovimientoInventarioService;
import com.transur.sigem.logistica.service.RepuestoService;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/logistica")
@RequiredArgsConstructor
public class LogisticaViewController {

    private final RepuestoService repuestoService;
    private final CategoriaRepuestoRepository categoriaRepository;
    private final AlmacenService almacenService;
    private final UbicacionAlmacenRepository ubicacionRepository;
    private final InventarioMaquinariaService inventarioMaquinariaService;
    private final MaquinariaRepository maquinariaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final MovimientoInventarioService movimientoService;
    private final OrdenCompraService ordenCompraService;
    private final ProveedorService proveedorService;
    private final RecepcionService recepcionService;

    @GetMapping({"", "/", "/dashboard", "/deshboard"})
    public String dashboard(Model model) {
        List<Repuesto> repuestos = repuestoService.listar();
        List<Repuesto> alertas = repuestos.stream()
                .filter(r -> Boolean.TRUE.equals(r.getEstado()))
                .filter(r -> r.getStockActual() != null && r.getStockMinimo() != null && r.getStockActual() <= r.getStockMinimo())
                .sorted(Comparator.comparing(Repuesto::getStockActual))
                .toList();

        var ordenes = ordenCompraService.listar();
        long ordenesPendientes = ordenes.stream()
                .filter(o -> "PENDIENTE".equals(o.getEstado()) || "APROBADA".equals(o.getEstado()))
                .count();

        LocalDate hoy = LocalDate.now();
        var recepciones = recepcionService.listar();
        long recepcionesHoy = recepciones.stream()
                .filter(r -> hoy.equals(r.getFechaRecepcion()))
                .count();

        BigDecimal valorInventario = calcularValorInventario(repuestos, ordenes);

        model.addAttribute("alertas", alertas.stream().limit(8).toList());
        model.addAttribute("alertasTotal", alertas.size());
        model.addAttribute("ordenesPendientes", ordenesPendientes);
        model.addAttribute("recepcionesHoy", recepcionesHoy);
        model.addAttribute("valorInventario", valorInventario);
        model.addAttribute("totalRepuestos", repuestos.stream().filter(r -> Boolean.TRUE.equals(r.getEstado())).count());
        return "logistica/dashboard";
    }

    @GetMapping("/inventario")
    public String inventario(Model model) {
        var repuestos = repuestoService.listar();
        var maquinaria = inventarioMaquinariaService.listar();
        var almacenes = almacenService.listar();

        model.addAttribute("repuestos", repuestos);
        model.addAttribute("inventarioMaquinaria", maquinaria);
        model.addAttribute("almacenes", almacenes);
        model.addAttribute("stockBajo", repuestos.stream()
                .filter(r -> r.getStockActual() != null && r.getStockMinimo() != null && r.getStockActual() <= r.getStockMinimo()).count());
        return "logistica/inventario";
    }

    @GetMapping("/repuestos")
    public String repuestos(Model model) {
        prepararRepuestos(model, new RepuestoRequest());
        return "logistica/repuestos";
    }

    @PostMapping("/repuestos")
    public String crearRepuesto(@Valid @ModelAttribute("repuesto") RepuestoRequest request,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepararRepuestos(model, request);
            return "logistica/repuestos";
        }
        try {
            var creado = repuestoService.crear(request);
            redirect.addFlashAttribute("mensaje", "Repuesto " + creado.getCodigo() + " creado correctamente.");
            return "redirect:/logistica/repuestos";
        } catch (Exception e) {
            prepararRepuestos(model, request);
            model.addAttribute("error", e.getMessage());
            return "logistica/repuestos";
        }
    }

    @PostMapping("/categorias")
    public String crearCategoria(@RequestParam String nombre,
                                 @RequestParam(required = false) String descripcion,
                                 RedirectAttributes redirect) {
        try {
            if (nombre == null || nombre.isBlank()) throw new RuntimeException("El nombre de la categoría es obligatorio");
            if (categoriaRepository.findByNombre(nombre.trim()).isPresent()) {
                throw new RuntimeException("Ya existe una categoría con ese nombre");
            }
            CategoriaRepuesto c = new CategoriaRepuesto();
            c.setNombre(nombre.trim());
            c.setDescripcion(descripcion);
            categoriaRepository.save(c);
            redirect.addFlashAttribute("mensaje", "Categoría creada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/logistica/repuestos";
    }

    @GetMapping("/ordenes-compra")
    public String ordenes(Model model) {
        var ordenes = ordenCompraService.listar();
        model.addAttribute("ordenes", ordenes);
        model.addAttribute("pendientes", ordenes.stream().filter(o -> "PENDIENTE".equals(o.getEstado())).count());
        model.addAttribute("aprobadas", ordenes.stream().filter(o -> "APROBADA".equals(o.getEstado())).count());
        model.addAttribute("recibidas", ordenes.stream().filter(o -> "RECIBIDA".equals(o.getEstado())).count());
        return "logistica/ordenes-compra";
    }

    @GetMapping("/ordenes-compra/nueva")
    public String nuevaOrden(Model model) {
        OrdenCompraRequest request = new OrdenCompraRequest();
        request.setFechaEmision(LocalDate.now());
        request.setDetalles(new ArrayList<>());
        request.getDetalles().add(new OrdenCompraDetalleDTO());
        prepararOrden(model, request);
        return "logistica/orden-form";
    }

    @PostMapping("/ordenes-compra")
    public String crearOrden(@Valid @ModelAttribute("orden") OrdenCompraRequest request,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirect) {
        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            result.reject("detalles", "Debe agregar al menos un detalle");
        }
        if (result.hasErrors()) {
            prepararOrden(model, request);
            return "logistica/orden-form";
        }
        try {
            var creado = ordenCompraService.crear(request);
            redirect.addFlashAttribute("mensaje", "Orden " + creado.getNumero() + " creada correctamente.");
            return "redirect:/logistica/ordenes-compra";
        } catch (Exception e) {
            prepararOrden(model, request);
            model.addAttribute("error", e.getMessage());
            return "logistica/orden-form";
        }
    }

    @GetMapping("/ordenes-compra/{id}/estado")
    public String cambiarEstadoOrdenGet(@PathVariable Long id, @RequestParam String estado, RedirectAttributes redirect) {
        return cambiarEstadoOrden(id, estado, redirect);
    }

    @PostMapping("/ordenes-compra/{id}/estado")
    public String cambiarEstadoOrden(@PathVariable Long id,
                                     @RequestParam String estado,
                                     RedirectAttributes redirect) {
        try {
            ordenCompraService.cambiarEstado(id, estado);
            redirect.addFlashAttribute("mensaje", "Estado de la orden actualizado correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/logistica/ordenes-compra";
    }

    @GetMapping("/recepcion")
    public String recepcion(Model model) {
        model.addAttribute("recepciones", recepcionService.listar());
        return "logistica/recepcion";
    }

    @GetMapping("/recepcion/nueva")
    public String nuevaRecepcion(Model model) {
        RecepcionRequest request = new RecepcionRequest();
        request.setFechaRecepcion(LocalDate.now());
        request.setDetalles(new ArrayList<>());
        request.getDetalles().add(new RecepcionDetalleDTO());
        prepararRecepcion(model, request);
        return "logistica/recepcion-form";
    }

    @PostMapping("/recepcion")
    public String crearRecepcion(@Valid @ModelAttribute("recepcion") RecepcionRequest request,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirect) {
        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            result.reject("detalles", "Debe agregar al menos un detalle");
        }
        if (result.hasErrors()) {
            prepararRecepcion(model, request);
            return "logistica/recepcion-form";
        }
        try {
            var creado = recepcionService.crear(request);
            redirect.addFlashAttribute("mensaje", "Recepción " + creado.getNumero() + " registrada. El stock fue actualizado.");
            return "redirect:/logistica/recepcion";
        } catch (Exception e) {
            prepararRecepcion(model, request);
            model.addAttribute("error", e.getMessage());
            return "logistica/recepcion-form";
        }
    }

    @GetMapping("/movimientos")
    public String movimientos(Model model) {
        model.addAttribute("movimientos", movimientoService.listarTodos());
        return "logistica/movimientos";
    }

    @GetMapping("/movimientos/nuevo")
    public String nuevoMovimiento(Model model) {
        MovimientoRequest request = new MovimientoRequest();
        model.addAttribute("movimiento", request);
        model.addAttribute("repuestos", repuestoService.listar());
        model.addAttribute("ubicaciones", ubicacionRepository.findAll());
        return "logistica/movimiento-form";
    }

    @PostMapping("/movimientos")
    public String registrarMovimiento(@Valid @ModelAttribute("movimiento") MovimientoRequest request,
                                      BindingResult result,
                                      Model model,
                                      RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("repuestos", repuestoService.listar());
            model.addAttribute("ubicaciones", ubicacionRepository.findAll());
            return "logistica/movimiento-form";
        }
        try {
            movimientoService.registrar(request);
            redirect.addFlashAttribute("mensaje", "Movimiento registrado y stock actualizado correctamente.");
            return "redirect:/logistica/movimientos";
        } catch (Exception e) {
            model.addAttribute("repuestos", repuestoService.listar());
            model.addAttribute("ubicaciones", ubicacionRepository.findAll());
            model.addAttribute("error", e.getMessage());
            return "logistica/movimiento-form";
        }
    }

    @GetMapping("/proveedores")
    public String proveedores(Model model) {
        model.addAttribute("proveedores", proveedorService.listar());
        model.addAttribute("proveedor", new ProveedorRequest());
        return "logistica/proveedores";
    }

    @PostMapping("/proveedores")
    public String crearProveedor(@Valid @ModelAttribute("proveedor") ProveedorRequest request,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("proveedores", proveedorService.listar());
            return "logistica/proveedores";
        }
        try {
            proveedorService.crear(request);
            redirect.addFlashAttribute("mensaje", "Proveedor creado correctamente.");
            return "redirect:/logistica/proveedores";
        } catch (Exception e) {
            model.addAttribute("proveedores", proveedorService.listar());
            model.addAttribute("error", e.getMessage());
            return "logistica/proveedores";
        }
    }

    @GetMapping("/almacenes")
    public String almacenes(Model model) {
        model.addAttribute("almacenes", almacenService.listar());
        model.addAttribute("almacen", new AlmacenRequest());
        model.addAttribute("ubicacion", new UbicacionRequest());
        model.addAttribute("empleados", empleadoRepository.findByEstadoTrue());
        return "logistica/almacenes";
    }

    @PostMapping("/almacenes")
    public String crearAlmacen(@Valid @ModelAttribute("almacen") AlmacenRequest request,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("almacenes", almacenService.listar());
            model.addAttribute("almacen", request);
            model.addAttribute("ubicacion", new UbicacionRequest());
            model.addAttribute("empleados", empleadoRepository.findByEstadoTrue());
            return "logistica/almacenes";
        }
        try {
            almacenService.crear(request);
            redirect.addFlashAttribute("mensaje", "Almacén creado correctamente.");
            return "redirect:/logistica/almacenes";
        } catch (Exception e) {
            model.addAttribute("almacenes", almacenService.listar());
            model.addAttribute("almacen", request);
            model.addAttribute("ubicacion", new UbicacionRequest());
            model.addAttribute("empleados", empleadoRepository.findByEstadoTrue());
            model.addAttribute("error", e.getMessage());
            return "logistica/almacenes";
        }
    }

    @PostMapping("/ubicaciones")
    public String crearUbicacion(@Valid @ModelAttribute("ubicacion") UbicacionRequest request,
                                 BindingResult result,
                                 RedirectAttributes redirect) {
        try {
            if (result.hasErrors()) throw new RuntimeException("Código y almacén son obligatorios");
            almacenService.crearUbicacion(request);
            redirect.addFlashAttribute("mensaje", "Ubicación creada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/logistica/almacenes";
    }

    @GetMapping("/inventario-maquinaria/nuevo")
    public String nuevoInventarioMaquinaria(Model model) {
        InventarioMaquinariaRequest request = new InventarioMaquinariaRequest();
        request.setFechaIngreso(LocalDate.now());
        request.setSituacionLogistica("EN_PATIO_CENTRAL");
        model.addAttribute("inventario", request);
        model.addAttribute("maquinarias", maquinariaRepository.findAll());
        model.addAttribute("ubicaciones", ubicacionRepository.findAll());
        return "logistica/inventario-maquinaria-form";
    }

    @PostMapping("/inventario-maquinaria")
    public String registrarInventarioMaquinaria(@Valid @ModelAttribute("inventario") InventarioMaquinariaRequest request,
                                                BindingResult result,
                                                Model model,
                                                RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("maquinarias", maquinariaRepository.findAll());
            model.addAttribute("ubicaciones", ubicacionRepository.findAll());
            return "logistica/inventario-maquinaria-form";
        }
        try {
            inventarioMaquinariaService.registrar(request);
            redirect.addFlashAttribute("mensaje", "Inventario físico de maquinaria registrado correctamente.");
            return "redirect:/logistica/inventario";
        } catch (Exception e) {
            model.addAttribute("maquinarias", maquinariaRepository.findAll());
            model.addAttribute("ubicaciones", ubicacionRepository.findAll());
            model.addAttribute("error", e.getMessage());
            return "logistica/inventario-maquinaria-form";
        }
    }

    private void prepararRepuestos(Model model, RepuestoRequest request) {
        model.addAttribute("repuestos", repuestoService.listar());
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("repuesto", request);
    }

    private void prepararOrden(Model model, OrdenCompraRequest request) {
        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            request.setDetalles(new ArrayList<>());
            request.getDetalles().add(new OrdenCompraDetalleDTO());
        }
        model.addAttribute("orden", request);
        model.addAttribute("proveedores", proveedorService.listar().stream().filter(p -> Boolean.TRUE.equals(p.getEstado())).toList());
        model.addAttribute("repuestos", repuestoService.listar().stream().filter(r -> Boolean.TRUE.equals(r.getEstado())).toList());
    }

    private void prepararRecepcion(Model model, RecepcionRequest request) {
        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            request.setDetalles(new ArrayList<>());
            request.getDetalles().add(new RecepcionDetalleDTO());
        }
        model.addAttribute("recepcion", request);
        model.addAttribute("ordenes", ordenCompraService.listar().stream()
                .filter(o -> "APROBADA".equals(o.getEstado()))
                .toList());
        model.addAttribute("repuestos", repuestoService.listar().stream().filter(r -> Boolean.TRUE.equals(r.getEstado())).toList());
    }

    private BigDecimal calcularValorInventario(List<Repuesto> repuestos, List<com.transur.sigem.compras.entity.OrdenCompra> ordenes) {
        Map<Long, BigDecimal> ultimoCosto = new HashMap<>();
        Map<Long, LocalDate> fechaCosto = new HashMap<>();

        for (var orden : ordenes) {
            if ("CANCELADA".equals(orden.getEstado()) || orden.getDetalles() == null) continue;
            for (OrdenCompraDetalle detalle : orden.getDetalles()) {
                if (detalle.getRepuesto() == null || detalle.getPrecioUnitario() == null) continue;
                Long repuestoId = detalle.getRepuesto().getId();
                LocalDate fecha = orden.getFechaEmision();
                if (!fechaCosto.containsKey(repuestoId) || fecha.isAfter(fechaCosto.get(repuestoId))) {
                    fechaCosto.put(repuestoId, fecha);
                    ultimoCosto.put(repuestoId, detalle.getPrecioUnitario());
                }
            }
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Repuesto repuesto : repuestos) {
            BigDecimal costo = ultimoCosto.getOrDefault(repuesto.getId(), BigDecimal.ZERO);
            total = total.add(costo.multiply(BigDecimal.valueOf(repuesto.getStockActual() == null ? 0 : repuesto.getStockActual())));
        }
        return total;
    }
}

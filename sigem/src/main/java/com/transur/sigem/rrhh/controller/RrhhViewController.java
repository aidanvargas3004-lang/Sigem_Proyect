package com.transur.sigem.rrhh.controller;

import com.transur.sigem.rrhh.dto.DocumentoRequest;
import com.transur.sigem.rrhh.dto.DocumentoResponse;
import com.transur.sigem.rrhh.dto.EmpleadoRequest;
import com.transur.sigem.rrhh.dto.EmpleadoResponse;
import com.transur.sigem.rrhh.entity.DocumentoEmpleado;
import com.transur.sigem.rrhh.entity.Empleado;
import com.transur.sigem.rrhh.repository.DocumentoEmpleadoRepository;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import com.transur.sigem.rrhh.service.DocumentoService;
import com.transur.sigem.rrhh.service.EmpleadoService;
import com.transur.sigem.sistema.entity.Notificacion;
import com.transur.sigem.sistema.repository.NotificacionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/rrhh")
@RequiredArgsConstructor
public class RrhhViewController {

    private final EmpleadoService empleadoService;
    private final DocumentoService documentoService;
    private final EmpleadoRepository empleadoRepository;
    private final DocumentoEmpleadoRepository documentoRepository;
    private final NotificacionRepository notificacionRepository;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        List<EmpleadoResponse> empleados = empleadoService.listarTodos();
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(30);

        List<DocumentoEmpleado> activos = documentoRepository.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getEstado()))
                .sorted(Comparator.comparing(DocumentoEmpleado::getFechaVencimiento))
                .toList();

        List<Map<String, Object>> alertas = construirAlertas(activos, hoy, limite);
        long vencidos = activos.stream().filter(d -> d.getFechaVencimiento().isBefore(hoy)).count();
        long porVencer = activos.stream().filter(d -> !d.getFechaVencimiento().isBefore(hoy) && !d.getFechaVencimiento().isAfter(limite)).count();

        model.addAttribute("active", "dashboard");
        model.addAttribute("personalActivo", empleados.stream().filter(e -> Boolean.TRUE.equals(e.getEstado())).count());
        model.addAttribute("documentosVencidos", vencidos);
        model.addAttribute("documentosPorVencer", porVencer);
        model.addAttribute("alertasNoLeidas", notificacionRepository.findAll().stream().filter(n -> Boolean.FALSE.equals(n.getLeida())).count());
        model.addAttribute("alertas", alertas.stream().limit(8).toList());
        return "rrhh/dashboard";
    }

    @GetMapping("/empleados")
    public String empleados(@RequestParam(required = false) String cargo,
                            @RequestParam(required = false) String q,
                            Model model) {
        List<EmpleadoResponse> lista = empleadoService.listarTodos();
        if (cargo != null && !cargo.isBlank()) {
            lista = lista.stream().filter(e -> cargo.equalsIgnoreCase(e.getCargo())).toList();
        }
        if (q != null && !q.isBlank()) {
            String buscado = q.toLowerCase(Locale.ROOT);
            lista = lista.stream().filter(e ->
                    safe(e.getDocumento()).contains(buscado) ||
                    safe(e.getNombres()).contains(buscado) ||
                    safe(e.getApellidos()).contains(buscado) ||
                    safe(e.getCargo()).contains(buscado)
            ).toList();
        }
        model.addAttribute("active", "empleados");
        model.addAttribute("empleados", lista);
        model.addAttribute("q", q);
        model.addAttribute("cargo", cargo);
        model.addAttribute("cargos", empleadoRepository.findAll().stream()
                .map(Empleado::getCargo).filter(Objects::nonNull).distinct().sorted().toList());
        return "rrhh/empleados";
    }

    @GetMapping("/empleados/nuevo")
    public String nuevoEmpleado(Model model) {
        if (!model.containsAttribute("empleadoRequest")) model.addAttribute("empleadoRequest", new EmpleadoRequest());
        model.addAttribute("active", "empleados");
        return "rrhh/empleado-form";
    }

    @PostMapping("/empleados/guardar")
    public String guardarEmpleado(@Valid @ModelAttribute("empleadoRequest") EmpleadoRequest request,
                                  BindingResult result, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("active", "empleados");
            return "rrhh/empleado-form";
        }
        try {
            empleadoService.crear(request);
            ra.addFlashAttribute("success", "Empleado registrado correctamente.");
            return "redirect:/rrhh/empleados";
        } catch (RuntimeException ex) {
            model.addAttribute("active", "empleados");
            model.addAttribute("error", ex.getMessage());
            return "rrhh/empleado-form";
        }
    }

    @PostMapping("/empleados/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam Boolean estado, RedirectAttributes ra) {
        empleadoService.cambiarEstado(id, estado);
        ra.addFlashAttribute("success", estado ? "Empleado habilitado." : "Empleado deshabilitado.");
        return "redirect:/rrhh/empleados";
    }

    @GetMapping("/documentacion")
    public String documentacion(@RequestParam(required = false) Long empleadoId, Model model) {
        List<EmpleadoResponse> empleados = empleadoService.listarTodos();
        EmpleadoResponse seleccionado = empleadoId == null ? null : empleados.stream().filter(e -> empleadoId.equals(e.getId())).findFirst().orElse(null);
        List<DocumentoResponse> documentos = empleadoId == null ? List.of() : documentoService.listarPorEmpleado(empleadoId);

        model.addAttribute("active", "documentacion");
        model.addAttribute("empleados", empleados);
        model.addAttribute("seleccionado", seleccionado);
        model.addAttribute("documentos", documentos);
        model.addAttribute("hoy", LocalDate.now());
        return "rrhh/documentacion";
    }

    @GetMapping("/documentacion/nuevo")
    public String nuevoDocumento(@RequestParam(required = false) Long empleadoId, Model model) {
        DocumentoRequest request = new DocumentoRequest();
        request.setEmpleadoId(empleadoId);
        model.addAttribute("active", "documentacion");
        model.addAttribute("documentoRequest", request);
        model.addAttribute("empleados", empleadoService.listarTodos());
        return "rrhh/documento-form";
    }

    @PostMapping("/documentacion/guardar")
    public String guardarDocumento(@Valid @ModelAttribute("documentoRequest") DocumentoRequest request,
                                   BindingResult result, RedirectAttributes ra, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("active", "documentacion");
            model.addAttribute("empleados", empleadoService.listarTodos());
            return "rrhh/documento-form";
        }
        try {
            documentoService.crear(request);
            ra.addFlashAttribute("success", "Documento registrado correctamente.");
            return "redirect:/rrhh/documentacion?empleadoId=" + request.getEmpleadoId();
        } catch (RuntimeException ex) {
            model.addAttribute("active", "documentacion");
            model.addAttribute("empleados", empleadoService.listarTodos());
            model.addAttribute("error", ex.getMessage());
            return "rrhh/documento-form";
        }
    }

    @GetMapping("/vencimientos")
    public String vencimientos(Model model) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(30);
        List<Map<String, Object>> filas = construirAlertas(documentoRepository.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getEstado()))
                .sorted(Comparator.comparing(DocumentoEmpleado::getFechaVencimiento))
                .toList(), hoy, limite);
        model.addAttribute("active", "vencimientos");
        model.addAttribute("filas", filas);
        return "rrhh/vencimientos";
    }

    @GetMapping("/alertas")
    public String alertas(Model model) {
        List<Notificacion> notificaciones = notificacionRepository.findAll().stream()
                .sorted(Comparator.comparing(Notificacion::getFecha, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
        model.addAttribute("active", "alertas");
        model.addAttribute("notificaciones", notificaciones);
        return "rrhh/alertas";
    }

    private List<Map<String, Object>> construirAlertas(List<DocumentoEmpleado> documentos, LocalDate hoy, LocalDate limite) {
        return documentos.stream().filter(d -> d.getFechaVencimiento() != null)
                .filter(d -> !d.getFechaVencimiento().isAfter(limite))
                .map(d -> {
                    long dias = ChronoUnit.DAYS.between(hoy, d.getFechaVencimiento());
                    Map<String, Object> m = new HashMap<>();
                    m.put("empleado", d.getEmpleado().getNombres() + " " + d.getEmpleado().getApellidos());
                    m.put("documento", d.getTipoDocumento());
                    m.put("fecha", d.getFechaVencimiento());
                    m.put("dias", dias);
                    m.put("estado", dias < 0 ? "VENCIDO" : (dias <= 7 ? "CRÍTICO" : (dias <= 15 ? "URGENTE" : "PRÓXIMO")));
                    return m;
                }).toList();
    }

    private String safe(String value) { return value == null ? "" : value.toLowerCase(Locale.ROOT); }
}

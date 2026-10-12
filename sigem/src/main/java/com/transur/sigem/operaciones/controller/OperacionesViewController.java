package com.transur.sigem.operaciones.controller;

import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.common.service.CloudinaryService;
import com.transur.sigem.mantenimiento.repository.MantenimientoRepository;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.dto.MaquinariaRequest;
import com.transur.sigem.maquinaria.dto.TarifaRequest;
import com.transur.sigem.maquinaria.dto.TipoMaquinariaRequest;
import com.transur.sigem.maquinaria.entity.TipoMaquinaria;
import com.transur.sigem.maquinaria.service.MaquinariaService;
import com.transur.sigem.maquinaria.service.TarifaService;
import com.transur.sigem.maquinaria.service.TipoMaquinariaService;
import com.transur.sigem.maquinaria.repository.TarifaRepository;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.operaciones.dto.AsignacionRequest;
import com.transur.sigem.operaciones.dto.ServicioRequest;
import com.transur.sigem.operaciones.entity.Asignacion;
import com.transur.sigem.operaciones.entity.Incidencia;
import com.transur.sigem.operaciones.entity.Servicio;
import com.transur.sigem.operaciones.entity.TipoServicio;
import com.transur.sigem.operaciones.repository.AsignacionRepository;
import com.transur.sigem.operaciones.repository.IncidenciaRepository;
import com.transur.sigem.operaciones.repository.ServicioRepository;
import com.transur.sigem.operaciones.repository.TipoServicioRepository;
import com.transur.sigem.operaciones.service.AsignacionService;
import com.transur.sigem.operaciones.service.ServicioService;
import com.transur.sigem.rrhh.entity.Empleado;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/operaciones")
@RequiredArgsConstructor
public class OperacionesViewController {

    private final ServicioRepository servicioRepository;
    private final ServicioService servicioService;
    private final AsignacionRepository asignacionRepository;
    private final AsignacionService asignacionService;
    private final IncidenciaRepository incidenciaRepository;
    private final TipoServicioRepository tipoServicioRepository;
    private final ContratoRepository contratoRepository;
    private final MaquinariaRepository maquinariaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final MantenimientoRepository mantenimientoRepository;
    private final MaquinariaService maquinariaService;
    private final TipoMaquinariaService tipoMaquinariaService;
    private final TarifaService tarifaService;
    private final TarifaRepository tarifaRepository;
    private final CloudinaryService cloudinaryService;

    @GetMapping({"", "/", "/dashboard", "/deshboard"})
    public String dashboard(Model model) {
        List<Servicio> servicios = cargarServicios();
        List<Asignacion> asignaciones = cargarAsignaciones();
        List<Maquinaria> maquinaria = maquinariaRepository.findAll();

        long pendientes = servicios.stream().filter(s -> "POR_ASIGNAR".equals(s.getEstado())).count();
        long enCurso = servicios.stream().filter(s -> "EN_CURSO".equals(s.getEstado())).count();
        long finalizados = servicios.stream().filter(s -> "FINALIZADO".equals(s.getEstado())).count();
        long asignacionesPendientes = servicios.stream().filter(s -> "POR_ASIGNAR".equals(s.getEstado())).count();
        long disponibles = maquinaria.stream().filter(m -> "OPERATIVO".equals(m.getEstadoOperativo())).count();
        long incidenciasAbiertas = incidenciaRepository.findByEstado("ABIERTA").size();

        model.addAttribute("serviciosPendientes", pendientes);
        model.addAttribute("serviciosEnCurso", enCurso);
        model.addAttribute("serviciosFinalizados", finalizados);
        model.addAttribute("asignacionesPendientes", asignacionesPendientes);
        model.addAttribute("unidadesDisponibles", disponibles);
        model.addAttribute("incidenciasAbiertas", incidenciasAbiertas);
        model.addAttribute("servicios", servicios.stream().limit(8).toList());
        model.addAttribute("ultimasAsignaciones", asignaciones.stream().limit(6).toList());
        return "operaciones/dashboard";
    }

    @GetMapping("/servicios")
    public String servicios(Model model) {
        model.addAttribute("servicios", cargarServicios());
        model.addAttribute("tiposServicio", tipoServicioRepository.findAll());
        return "operaciones/servicios";
    }

    @GetMapping("/servicios/nuevo")
    public String nuevoServicio(Model model) {
        model.addAttribute("servicio", new ServicioRequest());
        model.addAttribute("contratos", contratoRepository.findByTipoContratoOrderByFechaInicioDesc("SERVICIO"));
        model.addAttribute("tiposServicio", tipoServicioRepository.findAll());
        return "operaciones/servicio-form";
    }


    @PostMapping("/tipos-servicio")
    public String crearTipoServicio(@RequestParam String nombre, @RequestParam(required = false) String descripcion, @RequestParam(required = false) String imageUrl, RedirectAttributes redirect) {
        try {
            if (tipoServicioRepository.findByNombre(nombre.trim().toUpperCase()).isPresent()) throw new IllegalArgumentException("Ya existe ese tipo de servicio");
            TipoServicio t = new TipoServicio();
            t.setNombre(nombre.trim().toUpperCase());
            t.setDescripcion(descripcion);
            t.setImageUrl(imageUrl == null || imageUrl.isBlank() ? null : cloudinaryService.guardarUrlOImportar(imageUrl));
            t.setEstado(true);
            tipoServicioRepository.save(t);
            redirect.addFlashAttribute("mensaje", "Tipo de servicio creado correctamente.");
        } catch (Exception e) { redirect.addFlashAttribute("error", "No se pudo crear el tipo de servicio: " + e.getMessage()); }
        return "redirect:/operaciones/servicios";
    }

    @PostMapping("/tipos-servicio/{id}/imagen-url")
    public String guardarImagenUrlTipoServicio(@PathVariable Long id, @RequestParam String imageUrl, RedirectAttributes redirect) {
        try {
            TipoServicio tipo = tipoServicioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de servicio no encontrado."));
            tipo.setImageUrl(cloudinaryService.guardarUrlOImportar(imageUrl));
            tipoServicioRepository.save(tipo);
            redirect.addFlashAttribute("mensaje", "Imagen del servicio guardada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "No se pudo guardar la URL del servicio: " + e.getMessage());
        }
        return "redirect:/operaciones/servicios";
    }

    @PostMapping("/tipos-servicio/{id}/imagen")
    public String subirImagenTipoServicio(@PathVariable Long id, @RequestParam("file") MultipartFile file, RedirectAttributes redirect) {
        try {
            TipoServicio tipo = tipoServicioRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de servicio no encontrado."));
            tipo.setImageUrl(cloudinaryService.uploadImage(file));
            tipoServicioRepository.save(tipo);
            redirect.addFlashAttribute("mensaje", "Imagen del servicio actualizada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "No se pudo subir la imagen del servicio: " + e.getMessage());
        }
        return "redirect:/operaciones/servicios";
    }

    @PostMapping("/maquinaria/tipos/{id}/imagen-url")
    public String guardarImagenUrlTipoMaquinaria(@PathVariable Long id, @RequestParam String imageUrl, RedirectAttributes redirect) {
        try {
            var tipo = tipoMaquinariaService.obtener(id);
            tipo.setImageUrl(cloudinaryService.guardarUrlOImportar(imageUrl));
            tipoMaquinariaService.guardar(tipo);
            redirect.addFlashAttribute("mensaje", "Imagen del tipo de maquinaria guardada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "No se pudo guardar la URL del tipo: " + e.getMessage());
        }
        return "redirect:/operaciones/maquinaria";
    }

    @PostMapping("/maquinaria/tipos/{id}/imagen")
    public String subirImagenTipoMaquinaria(@PathVariable Long id, @RequestParam("file") MultipartFile file, RedirectAttributes redirect) {
        try {
            TipoMaquinaria tipo = tipoMaquinariaService.obtener(id);
            tipo.setImageUrl(cloudinaryService.uploadImage(file));
            tipoMaquinariaService.guardar(tipo);
            redirect.addFlashAttribute("mensaje", "Imagen del tipo de maquinaria actualizada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "No se pudo subir la imagen del tipo: " + e.getMessage());
        }
        return "redirect:/operaciones/maquinaria";
    }

    @PostMapping("/maquinaria/{id}/imagen-url")
    public String guardarImagenUrlMaquinaria(@PathVariable Long id, @RequestParam String imageUrl, RedirectAttributes redirect) {
        try {
            Maquinaria maquinaria = maquinariaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Maquinaria no encontrada."));
            maquinaria.setImageUrl(cloudinaryService.guardarUrlOImportar(imageUrl));
            maquinariaRepository.save(maquinaria);
            redirect.addFlashAttribute("mensaje", "Imagen de la maquinaria guardada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "No se pudo guardar la URL de la maquinaria: " + e.getMessage());
        }
        return "redirect:/operaciones/maquinaria";
    }

    @PostMapping("/maquinaria/{id}/imagen")
    public String subirImagenMaquinaria(@PathVariable Long id, @RequestParam("file") MultipartFile file, RedirectAttributes redirect) {
        try {
            Maquinaria maquinaria = maquinariaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Maquinaria no encontrada."));
            maquinaria.setImageUrl(cloudinaryService.uploadImage(file));
            maquinariaRepository.save(maquinaria);
            redirect.addFlashAttribute("mensaje", "Imagen de la maquinaria actualizada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "No se pudo subir la imagen de la maquinaria: " + e.getMessage());
        }
        return "redirect:/operaciones/maquinaria";
    }

    @PostMapping("/servicios")
    public String crearServicio(@ModelAttribute ServicioRequest request, RedirectAttributes redirect) {
        try {
            servicioService.crear(request);
            redirect.addFlashAttribute("mensaje", "Servicio creado correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "No se pudo crear el servicio: " + e.getMessage());
        }
        return "redirect:/operaciones/servicios";
    }

    @PostMapping("/servicios/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam String estado, RedirectAttributes redirect) {
        try {
            servicioService.actualizarEstado(id, estado);
            redirect.addFlashAttribute("mensaje", "Estado del servicio actualizado.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "No se pudo actualizar el estado: " + e.getMessage());
        }
        return "redirect:/operaciones/servicios";
    }

    @GetMapping("/programacion")
    public String programacion(@RequestParam(required = false) String semana, Model model) {
        LocalDate referencia = semana == null ? LocalDate.now() : parseDate(semana, LocalDate.now());
        LocalDate lunes = referencia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<LocalDate> dias = new ArrayList<>();
        for (int i = 0; i < 6; i++) dias.add(lunes.plusDays(i));

        List<Maquinaria> maquinarias = maquinariaRepository.findAll();
        List<Asignacion> asignaciones = cargarAsignaciones();
        Map<String, List<Asignacion>> mapa = new HashMap<>();
        for (Asignacion a : asignaciones) {
            if (a.getMaquinaria() == null) continue;
            LocalDate fecha = a.getServicio() != null && a.getServicio().getFechaProgramada() != null
                    ? a.getServicio().getFechaProgramada().toLocalDate()
                    : (a.getFechaAsignacion() != null ? a.getFechaAsignacion().toLocalDate() : null);
            if (fecha == null) continue;
            if (!fecha.isBefore(lunes) && fecha.isBefore(lunes.plusDays(6))) {
                mapa.computeIfAbsent(a.getMaquinaria().getId() + "-" + fecha, k -> new ArrayList<>()).add(a);
            }
        }

        model.addAttribute("lunes", lunes);
        model.addAttribute("domingo", lunes.plusDays(5));
        model.addAttribute("dias", dias);
        model.addAttribute("maquinarias", maquinarias);
        model.addAttribute("programacion", mapa);
        model.addAttribute("semanaAnterior", lunes.minusWeeks(1));
        model.addAttribute("semanaSiguiente", lunes.plusWeeks(1));
        model.addAttribute("serviciosPendientes", cargarServicios().stream().filter(s -> "POR_ASIGNAR".equals(s.getEstado())).toList());
        return "operaciones/programacion";
    }

    @GetMapping("/maquinaria")
    public String maquinaria(Model model) {
        List<Maquinaria> lista = maquinariaRepository.findAll();
        Map<Long, Long> mantenimientos = mantenimientoRepository.findAll().stream()
                .filter(m -> m.getMaquinaria() != null)
                .collect(Collectors.groupingBy(m -> m.getMaquinaria().getId(), Collectors.counting()));
        model.addAttribute("maquinarias", lista);
        model.addAttribute("mantenimientos", mantenimientos);
        model.addAttribute("tiposMaquinaria", tipoMaquinariaService.listar());
        model.addAttribute("tarifas", tarifaRepository.findAllWithMaquinaria());
        model.addAttribute("tipoMaquinariaRequest", new TipoMaquinariaRequest());
        model.addAttribute("maquinariaRequest", new MaquinariaRequest());
        model.addAttribute("tarifaRequest", new TarifaRequest());
        return "operaciones/maquinaria";
    }

    @PostMapping("/maquinaria/tipos")
    public String crearTipoMaquinaria(@ModelAttribute TipoMaquinariaRequest request, RedirectAttributes redirect) {
        try { tipoMaquinariaService.crear(request); redirect.addFlashAttribute("mensaje", "Tipo de maquinaria creado."); }
        catch (Exception e) { redirect.addFlashAttribute("error", "No se pudo crear el tipo: " + e.getMessage()); }
        return "redirect:/operaciones/maquinaria";
    }

    @PostMapping("/maquinaria/registrar")
    public String registrarMaquinaria(@ModelAttribute MaquinariaRequest request, RedirectAttributes redirect) {
        try {
            if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
                request.setImageUrl(cloudinaryService.guardarUrlOImportar(request.getImageUrl()));
            }
            maquinariaService.crear(request); redirect.addFlashAttribute("mensaje", "Maquinaria registrada correctamente."); }
        catch (Exception e) { redirect.addFlashAttribute("error", "No se pudo registrar la maquinaria: " + e.getMessage()); }
        return "redirect:/operaciones/maquinaria";
    }

    @PostMapping("/maquinaria/tarifas")
    public String crearTarifa(@ModelAttribute TarifaRequest request, RedirectAttributes redirect) {
        try { tarifaService.crear(request); redirect.addFlashAttribute("mensaje", "Tarifa registrada correctamente."); }
        catch (Exception e) { redirect.addFlashAttribute("error", "No se pudo registrar la tarifa: " + e.getMessage()); }
        return "redirect:/operaciones/maquinaria";
    }

    @GetMapping("/conductores")
    public String conductores(Model model) {
        List<Empleado> empleados = new ArrayList<>();
        empleados.addAll(empleadoRepository.findByCargo("CONDUCTOR"));
        empleados.addAll(empleadoRepository.findByCargo("OPERADOR"));
        if (empleados.isEmpty()) empleados = empleadoRepository.findByEstadoTrue();

        Map<Long, Long> activas = new HashMap<>();
        for (Asignacion a : cargarAsignaciones()) {
            if (a.getEmpleado() != null && "ACTIVA".equals(a.getEstado())) {
                activas.merge(a.getEmpleado().getId(), 1L, Long::sum);
            }
        }
        model.addAttribute("empleados", empleados.stream().distinct().toList());
        model.addAttribute("activas", activas);
        return "operaciones/conductores";
    }

    @GetMapping("/asignaciones")
    public String asignaciones(Model model) {
        model.addAttribute("asignaciones", cargarAsignaciones());
        model.addAttribute("servicios", cargarServicios().stream().filter(s -> "POR_ASIGNAR".equals(s.getEstado())).toList());
        model.addAttribute("maquinarias", maquinariaRepository.findByEstadoOperativo("OPERATIVO"));
        model.addAttribute("empleados", empleadoRepository.findByEstadoTrue());
        return "operaciones/asignaciones";
    }

    @PostMapping("/asignaciones")
    public String crearAsignacion(@ModelAttribute AsignacionRequest request, RedirectAttributes redirect) {
        try {
            asignacionService.crear(request);
            redirect.addFlashAttribute("mensaje", "Asignación registrada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "No se pudo registrar la asignación: " + e.getMessage());
        }
        return "redirect:/operaciones/asignaciones";
    }

    @PostMapping("/asignaciones/{id}/cancelar")
    public String cancelarAsignacion(@PathVariable Long id, @RequestParam(required = false) String motivo, RedirectAttributes redirect) {
        try {
            asignacionService.cancelar(id, motivo);
            redirect.addFlashAttribute("mensaje", "Asignación cancelada.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "No se pudo cancelar: " + e.getMessage());
        }
        return "redirect:/operaciones/asignaciones";
    }

    @GetMapping("/checklist")
    public String checklist(Model model) {
        List<Servicio> servicios = cargarServicios();
        List<Incidencia> incidencias = incidenciaRepository.findAll();
        Map<Long, Long> incidenciasPorServicio = incidencias.stream()
                .filter(i -> i.getServicio() != null)
                .collect(Collectors.groupingBy(i -> i.getServicio().getId(), Collectors.counting()));
        model.addAttribute("servicios", servicios);
        model.addAttribute("maquinarias", maquinariaRepository.findAll());
        model.addAttribute("incidencias", incidencias);
        model.addAttribute("incidenciasPorServicio", incidenciasPorServicio);
        return "operaciones/checklist";
    }

    @PostMapping("/incidencias")
    public String crearIncidencia(@RequestParam Long servicioId,
                                  @RequestParam(required = false) Long maquinariaId,
                                  @RequestParam String tipo,
                                  @RequestParam String descripcion,
                                  RedirectAttributes redirect) {
        try {
            Servicio servicio = servicioRepository.findById(servicioId).orElseThrow(() -> new RuntimeException("Servicio no encontrado"));
            Incidencia incidencia = new Incidencia();
            incidencia.setServicio(servicio);
            if (maquinariaId != null) {
                maquinariaRepository.findById(maquinariaId).ifPresent(incidencia::setMaquinaria);
            }
            incidencia.setTipo(tipo);
            incidencia.setDescripcion(descripcion);
            incidencia.setFecha(LocalDateTime.now());
            incidencia.setEstado("ABIERTA");
            incidenciaRepository.save(incidencia);
            redirect.addFlashAttribute("mensaje", "Incidencia reportada correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "No se pudo registrar la incidencia: " + e.getMessage());
        }
        return "redirect:/operaciones/checklist";
    }

    @PostMapping("/incidencias/{id}/estado")
    public String estadoIncidencia(@PathVariable Long id, @RequestParam String estado, RedirectAttributes redirect) {
        incidenciaRepository.findById(id).ifPresent(i -> {
            i.setEstado(estado);
            incidenciaRepository.save(i);
        });
        redirect.addFlashAttribute("mensaje", "Incidencia actualizada.");
        return "redirect:/operaciones/checklist";
    }

    private List<Servicio> cargarServicios() {
        List<Servicio> servicios = servicioRepository.findAll();
        servicios.forEach(s -> {
            if (s.getContrato() != null && s.getContrato().getCliente() != null) {
                s.getContrato().getCliente().getRazonSocial();
            }
            if (s.getTipoServicio() != null) s.getTipoServicio().getNombre();
        });
        servicios.sort(Comparator.comparing(Servicio::getFechaProgramada, Comparator.nullsLast(Comparator.reverseOrder())));
        return servicios;
    }

    private List<Asignacion> cargarAsignaciones() {
        List<Asignacion> asignaciones = asignacionRepository.findAll();
        asignaciones.forEach(a -> {
            if (a.getServicio() != null) {
                a.getServicio().getId();
                if (a.getServicio().getContrato() != null && a.getServicio().getContrato().getCliente() != null) {
                    a.getServicio().getContrato().getCliente().getRazonSocial();
                }
            }
            if (a.getMaquinaria() != null) a.getMaquinaria().getCodigoInterno();
            if (a.getEmpleado() != null) a.getEmpleado().getNombres();
        });
        asignaciones.sort(Comparator.comparing(Asignacion::getFechaAsignacion, Comparator.nullsLast(Comparator.reverseOrder())));
        return asignaciones;
    }

    private LocalDate parseDate(String value, LocalDate fallback) {
        try { return LocalDate.parse(value); } catch (Exception e) { return fallback; }
    }
}

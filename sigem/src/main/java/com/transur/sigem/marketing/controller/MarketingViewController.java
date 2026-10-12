package com.transur.sigem.marketing.controller;

import com.transur.sigem.cliente.dto.ClienteRequest;
import com.transur.sigem.cliente.dto.ClienteResponse;
import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.cliente.service.ClienteService;
import com.transur.sigem.maquinaria.service.MaquinariaService;
import com.transur.sigem.marketing.dto.CampaniaRequest;
import com.transur.sigem.marketing.dto.LeadRequest;
import com.transur.sigem.marketing.dto.OportunidadRequest;
import com.transur.sigem.marketing.entity.Campania;
import com.transur.sigem.marketing.entity.Lead;
import com.transur.sigem.marketing.entity.Oportunidad;
import com.transur.sigem.marketing.service.CampaniaService;
import com.transur.sigem.marketing.service.LeadService;
import com.transur.sigem.marketing.service.OportunidadService;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.UsuarioRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/marketing")
@RequiredArgsConstructor
public class MarketingViewController {

    private final LeadService leadService;
    private final CampaniaService campaniaService;
    private final OportunidadService oportunidadService;
    private final ClienteService clienteService;
    private final MaquinariaService servicioService;
    private final UsuarioRepository usuarioRepository;



    /**
     * Dashboard Principal de Marketing
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // KPIs para tarjetas superiores
        model.addAttribute("totalLeads", leadService.contarTotales());
        model.addAttribute("oportunidadesActivas", oportunidadService.contarActivas());
        model.addAttribute("campaniasActivas", campaniaService.contarActivas());

        // Cálculos derivados
        long total = leadService.contarTotales();
        long convertidos = leadService.contarPorEstado("CONVERTIDO");
        double tasa = total > 0 ? (convertidos * 100.0) / total : 0;

        model.addAttribute("tasaConversion", String.format("%.1f%%", tasa));
        model.addAttribute("valorPipeline", oportunidadService.sumarValoresActivos());

        // Tabla de últimos leads
        model.addAttribute("ultimosLeads", leadService.listarUltimos(5));

        long totalOpps = oportunidadService.contarTodas();
        model.addAttribute("oportunidadesActivas", totalOpps);

        return "marketing/dashboard";
    }

    /**
     * Gestión de Campañas
     */
    @GetMapping("/campanias")
    public String campanias(Model model) {
        model.addAttribute("campanias", campaniaService.listar());
        return "marketing/campanias";
    }

    /**
     * Base de Datos de Leads
     */

    /**
     * Pipeline de Oportunidades
     */
    @GetMapping("/oportunidades")
    public String oportunidades(Model model) {
        model.addAttribute("oportunidades", oportunidadService.listar());
        model.addAttribute("valorTotalPipeline", oportunidadService.sumarValoresActivos());
        model.addAttribute("probabilidadPromedio", oportunidadService.calcularProbabilidadPromedio());
        return "marketing/oportunidades";
    }

    /**
     * Directorio de Clientes y Prospectos
     */
    @GetMapping("/clientes")
    public String clientes(Model model) {
        model.addAttribute("clientesProspectos", clienteService.listarDirectorio());
        return "marketing/clientes";
    }

    /**
     * Reportes de Rendimiento (Gráficos)
     */

    // En MarketingViewController.java

    @GetMapping("/campanias/nuevo")
    public String formNuevaCampania(Model model) {
        model.addAttribute("campania", new CampaniaRequest());
        model.addAttribute("isEdit", false); // ✅ ESTO FALTABA: Inicializa isEdit como falso
        return "marketing/campania-form";
    }

    @PostMapping("/campanias/guardar")
    public String guardarCampania(@Valid @ModelAttribute("campania") CampaniaRequest request,
                                  BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "marketing/campania-form";
        }

        try {
            campaniaService.crearCampania(request);
            return "redirect:/marketing/campanias?success=true";
        } catch (Exception e) {
            model.addAttribute("msgError", "Error al guardar: " + e.getMessage());
            return "marketing/campania-form";
        }
    }

    @GetMapping("/campanias/{id}/editar")
    public String formEditarCampania(@PathVariable Long id, Model model) {
        // Buscamos la campaña por ID (asumiendo que tienes findById en tu service o repo)
        Campania campania = campaniaService.obtenerPorId(id);

        if (campania == null) {
            return "redirect:/marketing/campanias?error=not_found";
        }

        // Creamos un DTO y copiamos los datos de la entidad
        CampaniaRequest request = new CampaniaRequest();
        request.setNombre(campania.getNombre());
        request.setDescripcion(campania.getDescripcion());
        request.setFechaInicio(campania.getFechaInicio());
        request.setFechaFin(campania.getFechaFin());
        request.setPresupuesto(campania.getPresupuesto());
        request.setEstado(campania.getEstado());

        model.addAttribute("campania", request);
        model.addAttribute("isEdit", true); // Bandera para saber que estamos editando
        model.addAttribute("campaniaId", id); // ID para usar en el action del form

        return "marketing/campania-form"; // Reutilizamos el mismo HTML del formulario
    }

    @PostMapping("/campanias/{id}/actualizar")
    public String actualizarCampania(@PathVariable Long id,
                                     @Valid @ModelAttribute("campania") CampaniaRequest request,
                                     BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("campaniaId", id);
            return "marketing/campania-form";
        }

        try {
            // Aquí deberías tener un método update en tu service
            campaniaService.actualizarCampania(id, request);
            return "redirect:/marketing/campanias?success=updated";
        } catch (Exception e) {
            model.addAttribute("msgError", "Error al actualizar: " + e.getMessage());
            model.addAttribute("isEdit", true);
            model.addAttribute("campaniaId", id);
            return "marketing/campania-form";
        }
    }

    // FORMULARIO NUEVO LEAD
    @GetMapping("/leads/nuevo")
    public String formNuevoLead(Model model) {
        model.addAttribute("lead", new LeadRequest());
        model.addAttribute("isEdit", false);

        // ✅ Cargar servicios desde tu repositorio/service de maquinaria/servicios
        model.addAttribute("servicios", servicioService.listarActivos());

        return "marketing/lead-form";
    }

    // GUARDAR NUEVO LEAD
    @PostMapping("/leads/guardar")
    public String guardarLead(@Valid @ModelAttribute("lead") LeadRequest request,
                              BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "marketing/lead-form";
        }

        try {
            leadService.crear(request);
            return "redirect:/marketing/leads?success=created";
        } catch (Exception e) {
            model.addAttribute("msgError", e.getMessage());
            model.addAttribute("isEdit", false);
            return "marketing/lead-form";
        }
    }

    // FORMULARIO EDITAR LEAD
    @GetMapping("/leads/{id}/editar")
    public String formEditarLead(@PathVariable Long id, Model model) {
        Lead lead = leadService.obtenerPorId(id);
        if (lead == null) return "redirect:/marketing/leads?error=not_found";

        // Mapear entidad a DTO si usas DTOs, sino pasa la entidad directo
        LeadRequest request = new LeadRequest();
        request.setEmpresa(lead.getEmpresa());
        request.setNombres(lead.getNombres());
        request.setEmail(lead.getEmail());
        request.setTelefono(lead.getTelefono());
        if (lead.getServicio() != null) {
            request.setServicioId(lead.getServicio().getId());
        } else {
            request.setServicioId(null);
        }
        request.setOrigen(lead.getOrigen());
        request.setEstado(lead.getEstado());

        model.addAttribute("lead", request);
        model.addAttribute("isEdit", true);
        model.addAttribute("leadId", id);

        return "marketing/lead-form";
    }

    // ACTUALIZAR LEAD
    @PostMapping("/leads/{id}/actualizar")
    public String actualizarLead(@PathVariable Long id,
                                 @Valid @ModelAttribute("lead") LeadRequest request,
                                 BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("leadId", id);
            return "marketing/lead-form";
        }

        try {
            leadService.actualizar(id, request);
            return "redirect:/marketing/leads?success=updated";
        } catch (Exception e) {
            model.addAttribute("msgError", e.getMessage());
            model.addAttribute("isEdit", true);
            model.addAttribute("leadId", id);
            return "marketing/lead-form";
        }
    }
    @GetMapping("/leads")
    public String leads(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.trim().isEmpty()) {
            // Si hay búsqueda, usa el método de filtrado
            model.addAttribute("leads", leadService.buscarLeads(search.trim()));
            model.addAttribute("searchTerm", search);
        } else {
            // Si no hay búsqueda, lista todos
            model.addAttribute("leads", leadService.listar());
            model.addAttribute("searchTerm", "");
        }

        return "marketing/leads";
    }

    @GetMapping("/oportunidades/nuevo")
    public String formNuevaOportunidad(Model model) {
        model.addAttribute("oportunidad", new OportunidadRequest());
        model.addAttribute("isEdit", false); // Crucial para el HTML
        model.addAttribute("clientes", clienteService.listarDirectorio());
        return "marketing/oportunidad-form";
    }

    @PostMapping("/oportunidades/guardar")
    public String guardarOportunidad(@Valid @ModelAttribute("oportunidad") OportunidadRequest request,
                                     BindingResult result, Model model) {
        if (result.hasErrors()) {
            // Si hay errores de validación, recargar clientes y volver al form
            model.addAttribute("clientes", clienteService.listarDirectorio());
            return "marketing/oportunidad-form";
        }

        try {
            oportunidadService.crear(request);
            return "redirect:/marketing/oportunidades?success=created";
        } catch (Exception e) {
            model.addAttribute("msgError", e.getMessage());
            model.addAttribute("clientes", clienteService.listarDirectorio());
            return "marketing/oportunidad-form";
        }
    }

    @GetMapping("/oportunidades/{id}/editar")
    public String formEditarOportunidad(@PathVariable Long id, Model model) {
        // 1. Buscar la oportunidad existente
        Oportunidad opp = oportunidadService.obtenerPorId(id); // Asegúrate de tener este método en el service

        if (opp == null) {
            return "redirect:/marketing/oportunidades?error=not_found";
        }

        // 2. Mapear Entidad -> DTO para el formulario
        OportunidadRequest request = new OportunidadRequest();
        request.setDescripcion(opp.getDescripcion());
        request.setClienteId(opp.getCliente() != null ? opp.getCliente().getId() : null);
        request.setEstado(opp.getEstado());
        request.setMontoEstimado(opp.getMontoEstimado());
        request.setProbabilidad(opp.getProbabilidad());
        // Si tienes fechaCierreEstimado en la entidad, mapealo aquí también

        // 3. Enviar al modelo
        model.addAttribute("oportunidad", request);
        model.addAttribute("isEdit", true); // Bandera para cambiar título/botón en el form
        model.addAttribute("oportunidadId", id);
        model.addAttribute("clientes", clienteService.listarDirectorio()); // Para el select de clientes

        return "marketing/oportunidad-form"; // Reutilizamos el mismo form de crear
    }

    @PostMapping("/oportunidades/{id}/actualizar")
    public String actualizarOportunidad(@PathVariable Long id,
                                        @Valid @ModelAttribute("oportunidad") OportunidadRequest request,
                                        BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("oportunidadId", id);
            model.addAttribute("clientes", clienteService.listarDirectorio());
            return "marketing/oportunidad-form";
        }

        try {
            // ✅ ASEGÚRATE DE PASAR EL 'id' AL SERVICE
            oportunidadService.actualizar(id, request);
            return "redirect:/marketing/oportunidades?success=updated";
        } catch (Exception e) {
            model.addAttribute("msgError", e.getMessage());
            model.addAttribute("isEdit", true);
            model.addAttribute("oportunidadId", id);
            model.addAttribute("clientes", clienteService.listarDirectorio());
            return "marketing/oportunidad-form";
        }
    }

    @GetMapping("/clientes/nuevo")
    public String formNuevoCliente(Model model) {
        // ✅ CREAR OBJETO LIMPIO CON VALORES POR DEFECTO
        ClienteRequest request = new ClienteRequest();
        request.setUsername("");
        request.setNewPassword("");
        request.setConfirmPassword("");

        model.addAttribute("cliente", request);
        model.addAttribute("isEdit", false);
        return "marketing/cliente-form";
    }

    @PostMapping("/clientes/guardar")
    public String guardarCliente(@ModelAttribute("cliente") ClienteRequest request,
                                 BindingResult result, Model model) {

        // ✅ VALIDACIÓN MANUAL DE CONTRASEÑAS
        if (request.getNewPassword() == null || !request.getNewPassword().equals(request.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.password", "Las contraseñas no coinciden");
        }
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            result.rejectValue("username", "error.username", "El nombre de usuario es obligatorio");
        }

        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "marketing/cliente-form";
        }

        try {
            clienteService.crearClienteConUsuario(request);
            return "redirect:/marketing/clientes?success=created";
        } catch (Exception e) {
            model.addAttribute("msgError", e.getMessage());
            model.addAttribute("isEdit", false);
            return "marketing/cliente-form";
        }
    }

    @GetMapping("/clientes/{id}/editar")
    public String formEditarCliente(@PathVariable Long id, Model model) {
        try {
            ClienteResponse clienteDto = clienteService.obtenerPorId(id);

            // Buscar usuario asociado SOLO para obtener el username actual
            Usuario usuarioAsociado = usuarioRepository.findByClienteId(clienteDto.getId()).orElse(null);

            ClienteRequest request = new ClienteRequest();
            request.setDocumento(clienteDto.getDocumento());
            request.setRazonSocial(clienteDto.getRazonSocial());
            request.setTipoCliente(clienteDto.getTipoCliente());
            request.setDireccion(clienteDto.getDireccion());
            request.setTelefono(clienteDto.getTelefono());
            request.setEmail(clienteDto.getEmail());

            // ✅ PRE-LLENAR SOLO EL USERNAME ACTUAL, NUNCA LA CONTRASEÑA
            if (usuarioAsociado != null) {
                request.setUsername(usuarioAsociado.getUsername());
                // request.setPassword(...) <- ¡JAMÁS HAGAS ESTO!
            }

            model.addAttribute("cliente", request);
            model.addAttribute("isEdit", true);
            model.addAttribute("clienteId", id);

            return "marketing/cliente-form";
        } catch (RuntimeException e) {
            return "redirect:/marketing/clientes?error=not_found";
        }
    }

    @PostMapping("/clientes/{id}/actualizar")
    public String actualizarCliente(@PathVariable Long id,
                                    @Valid @ModelAttribute("cliente") ClienteRequest request,
                                    BindingResult result, Model model) {

        // ✅ AGREGA ESTO TEMPORALMENTE PARA VER SI HAY ERRORES DE VALIDACIÓN
        if (result.hasErrors()) {
            System.out.println(" ERRORES DE VALIDACIÓN EN EDICIÓN:");
            result.getAllErrors().forEach(error ->
                    System.out.println("   - " + error.getDefaultMessage())
            );

            model.addAttribute("isEdit", true);
            model.addAttribute("clienteId", id);
            // Asegúrate de pasar cualquier variable que el form necesite para renderizarse
            return "marketing/cliente-form";
        }

        try {
            System.out.println("✅ ACTUALIZANDO CLIENTE ID: " + id);
            clienteService.actualizar(id, request);
            return "redirect:/marketing/clientes?success=updated";
        } catch (Exception e) {
            System.out.println("❌ ERROR AL ACTUALIZAR: " + e.getMessage());
            e.printStackTrace(); // Esto imprimirá el stack trace completo en consola

            model.addAttribute("msgError", e.getMessage());
            model.addAttribute("isEdit", true);
            model.addAttribute("clienteId", id);
            return "marketing/cliente-form";
        }
    }


}
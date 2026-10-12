package com.transur.sigem.controller;

import com.transur.sigem.cliente.repository.ClienteRepository;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.operaciones.repository.ServicioRepository;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import com.transur.sigem.security.repository.RolRepository;
import com.transur.sigem.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final ClienteRepository clienteRepository;
    private final MaquinariaRepository maquinariaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ServicioRepository servicioRepository;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("usuarios", usuarioRepository.count());
        model.addAttribute("roles", rolRepository.count());
        model.addAttribute("clientes", clienteRepository.count());
        model.addAttribute("maquinarias", maquinariaRepository.count());
        model.addAttribute("empleados", empleadoRepository.findByEstadoTrue().size());
        model.addAttribute("servicios", servicioRepository.count());
        model.addAttribute("tituloPagina", "Administración SIGEM");
        return "dashboard/dashboard";
    }
}

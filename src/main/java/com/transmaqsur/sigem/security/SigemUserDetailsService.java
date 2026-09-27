package com.transmaqsur.sigem.security;

import com.transmaqsur.sigem.model.Usuario;
import com.transmaqsur.sigem.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SigemUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + u.getRol().getNombre()));
        u.getRol().getPermisos().forEach(p -> authorities.add(new SimpleGrantedAuthority(p)));
        return new UsuarioPrincipal(u.getId(), u.getUsername(), u.getPassword(), u.isActivo(),
                u.getNombreCompleto(), u.getRol().getNombre(),
                u.getEmpleado() != null ? u.getEmpleado().getId() : null, authorities);
    }
}

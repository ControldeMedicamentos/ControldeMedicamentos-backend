package com.medicamentos.security;

import com.medicamentos.domain.model.Usuario;
import com.medicamentos.domain.model.RolVistaPermiso;
import com.medicamentos.repository.RoleRepository;
import com.medicamentos.repository.RolVistaPermisoRepository;
import com.medicamentos.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final RolVistaPermisoRepository rolVistaPermisoRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
        roleRepository.findByName("ROLE_" + usuario.getRol().name())
                .ifPresent(role -> rolVistaPermisoRepository.findByRolId(role.getId())
                        .forEach(permiso -> addAuthorities(authorities, permiso)));

        return new User(
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getActivo(),
                true,
                true,
                true,
                authorities
        );
    }

    private void addAuthorities(
            Set<SimpleGrantedAuthority> authorities,
            RolVistaPermiso permiso) {
        String codigo = permiso.getVista().getCodigo();
        if (permiso.isLeer()) authorities.add(new SimpleGrantedAuthority(codigo + ":LEER"));
        if (permiso.isEscribir()) authorities.add(new SimpleGrantedAuthority(codigo + ":ESCRIBIR"));
        if (permiso.isModificar()) authorities.add(new SimpleGrantedAuthority(codigo + ":MODIFICAR"));
        if (permiso.isEliminar()) authorities.add(new SimpleGrantedAuthority(codigo + ":ELIMINAR"));
    }
}

package com.medicamentos.security;

import com.medicamentos.domain.enums.RolUsuario;
import com.medicamentos.domain.model.Role;
import com.medicamentos.domain.model.RolVistaPermiso;
import com.medicamentos.domain.model.Usuario;
import com.medicamentos.domain.model.Vista;
import com.medicamentos.repository.RoleRepository;
import com.medicamentos.repository.RolVistaPermisoRepository;
import com.medicamentos.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RolVistaPermisoRepository rolVistaPermisoRepository;

    @InjectMocks
    private UserDetailsServiceImpl service;

    @Test
    void cargaRolYPermisosComoAuthoritiesDelBackend() {
        Usuario usuario = new Usuario();
        usuario.setUsername("medico1");
        usuario.setPassword("hash");
        usuario.setActivo(true);
        usuario.setRol(RolUsuario.MEDICO);

        Role role = new Role();
        role.setId(2);
        role.setName("ROLE_MEDICO");

        Vista pacientes = new Vista();
        pacientes.setCodigo("VISTA_PACIENTES");

        RolVistaPermiso permiso = new RolVistaPermiso();
        permiso.setRol(role);
        permiso.setVista(pacientes);
        permiso.setLeer(true);
        permiso.setEscribir(true);

        when(usuarioRepository.findByUsername("medico1")).thenReturn(Optional.of(usuario));
        when(roleRepository.findByName("ROLE_MEDICO")).thenReturn(Optional.of(role));
        when(rolVistaPermisoRepository.findByRolId(2)).thenReturn(List.of(permiso));

        UserDetails result = service.loadUserByUsername("medico1");

        assertThat(result.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder(
                        "ROLE_MEDICO",
                        "VISTA_PACIENTES:LEER",
                        "VISTA_PACIENTES:ESCRIBIR"
                )
                .doesNotContain("VISTA_PACIENTES:ELIMINAR");
    }
}

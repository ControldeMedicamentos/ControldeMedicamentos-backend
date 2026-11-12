package com.medicamentos.config;

import com.medicamentos.domain.model.Role;
import com.medicamentos.domain.model.RolVistaPermiso;
import com.medicamentos.domain.model.Vista;
import com.medicamentos.repository.RoleRepository;
import com.medicamentos.repository.RolVistaPermisoRepository;
import com.medicamentos.repository.UsuarioRepository;
import com.medicamentos.repository.VistaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private VistaRepository vistaRepository;

    @Mock
    private RolVistaPermisoRepository rolVistaPermisoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataSeeder seeder;

    @Test
    void creaTresRolesYPermisosIniciales() {
        Map<String, Role> roles = Map.of(
                "ROLE_ADMIN", role(1, "ROLE_ADMIN"),
                "ROLE_MEDICO", role(2, "ROLE_MEDICO"),
                "ROLE_ENFERMERA", role(3, "ROLE_ENFERMERA")
        );
        Map<String, Vista> vistas = Map.of(
                "VISTA_DASHBOARD", vista(1, "VISTA_DASHBOARD"),
                "VISTA_PACIENTES", vista(2, "VISTA_PACIENTES"),
                "VISTA_ATENCIONES", vista(3, "VISTA_ATENCIONES"),
                "VISTA_MEDICAMENTOS", vista(4, "VISTA_MEDICAMENTOS"),
                "VISTA_INVENTARIO", vista(5, "VISTA_INVENTARIO"),
                "VISTA_REPORTES", vista(6, "VISTA_REPORTES")
        );

        when(roleRepository.findByName(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(roles.get(invocation.getArgument(0))));
        when(vistaRepository.existsByCodigo(anyString())).thenReturn(true);
        when(vistaRepository.findByCodigo(anyString()))
                .thenAnswer(invocation -> Optional.ofNullable(vistas.get(invocation.getArgument(0))));
        when(rolVistaPermisoRepository.findByRolIdAndVistaId(anyInt(), anyInt()))
                .thenReturn(Optional.empty());
        ReflectionTestUtils.setField(seeder, "bootstrapAdminEnabled", false);

        seeder.seed();

        ArgumentCaptor<Role> roleCaptor = ArgumentCaptor.forClass(Role.class);
        verify(roleRepository, times(3)).save(roleCaptor.capture());
        assertThat(roleCaptor.getAllValues()).extracting(Role::getName)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_MEDICO", "ROLE_ENFERMERA");

        ArgumentCaptor<RolVistaPermiso> permisoCaptor = ArgumentCaptor.forClass(RolVistaPermiso.class);
        verify(rolVistaPermisoRepository, times(16)).save(permisoCaptor.capture());
        List<RolVistaPermiso> admin = permisoCaptor.getAllValues().stream()
                .filter(permiso -> "ROLE_ADMIN".equals(permiso.getRol().getName()))
                .toList();
        assertThat(admin).hasSize(6).allSatisfy(permiso -> {
            assertThat(permiso.isLeer()).isTrue();
            assertThat(permiso.isEscribir()).isTrue();
            assertThat(permiso.isModificar()).isTrue();
            assertThat(permiso.isEliminar()).isTrue();
        });
        assertThat(permisoCaptor.getAllValues()).filteredOn(permiso ->
                "ROLE_MEDICO".equals(permiso.getRol().getName())
                        && "VISTA_INVENTARIO".equals(permiso.getVista().getCodigo()))
                .singleElement().satisfies(permiso -> assertThat(permiso.isLeer()).isFalse());
        assertThat(permisoCaptor.getAllValues()).filteredOn(permiso ->
                "ROLE_ENFERMERA".equals(permiso.getRol().getName())
                        && "VISTA_PACIENTES".equals(permiso.getVista().getCodigo()))
                .singleElement().satisfies(permiso -> assertThat(permiso.isModificar()).isFalse());
    }

    @Test
    void migraSoloLaMatrizMedicaInicialAnterior() {
        Role medico = role(2, "ROLE_MEDICO");
        List<RolVistaPermiso> permisos = new ArrayList<>(legacyPermissions(medico));
        permisos.add(permission(medico, "VISTA_REPORTES", false, false, false));
        when(roleRepository.findByName("ROLE_MEDICO")).thenReturn(Optional.of(medico));
        when(rolVistaPermisoRepository.findByRolId(2)).thenReturn(permisos);

        ReflectionTestUtils.invokeMethod(seeder, "migrateLegacyRole", "ROLE_MEDICO");

        RolVistaPermiso inventario = permisos.get(4);
        assertThat(inventario.isLeer()).isFalse();
        verify(rolVistaPermisoRepository).save(inventario);
    }

    @Test
    void conservaPermisosQueUnAdministradorYaPersonalizo() {
        Role enfermera = role(3, "ROLE_ENFERMERA");
        List<RolVistaPermiso> permisos = legacyPermissions(enfermera);
        permisos.get(1).setModificar(false);
        when(roleRepository.findByName("ROLE_ENFERMERA")).thenReturn(Optional.of(enfermera));
        when(rolVistaPermisoRepository.findByRolId(3)).thenReturn(permisos);

        ReflectionTestUtils.invokeMethod(seeder, "migrateLegacyRole", "ROLE_ENFERMERA");

        assertThat(permisos.get(2).isModificar()).isTrue();
        verify(rolVistaPermisoRepository, never()).save(any(RolVistaPermiso.class));
    }

    private List<RolVistaPermiso> legacyPermissions(Role role) {
        return List.of(
                permission(role, "VISTA_DASHBOARD", true, false, false),
                permission(role, "VISTA_PACIENTES", true, true, true),
                permission(role, "VISTA_ATENCIONES", true, true, true),
                permission(role, "VISTA_MEDICAMENTOS", true, false, false),
                permission(role, "VISTA_INVENTARIO", true, false, false)
        );
    }

    private RolVistaPermiso permission(Role role, String codigo, boolean leer,
                                       boolean escribir, boolean modificar) {
        RolVistaPermiso permiso = new RolVistaPermiso();
        permiso.setRol(role);
        permiso.setVista(vista(1, codigo));
        permiso.setLeer(leer);
        permiso.setEscribir(escribir);
        permiso.setModificar(modificar);
        return permiso;
    }

    private Role role(Integer id, String name) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        return role;
    }

    private Vista vista(Integer id, String codigo) {
        Vista vista = new Vista();
        vista.setId(id);
        vista.setCodigo(codigo);
        return vista;
    }
}

package com.medicamentos.service.Impl;

import com.medicamentos.domain.model.Role;
import com.medicamentos.repository.RoleRepository;
import com.medicamentos.repository.RolVistaPermisoRepository;
import com.medicamentos.repository.VistaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private VistaRepository vistaRepository;

    @Mock
    private RolVistaPermisoRepository rolVistaPermisoRepository;

    @InjectMocks
    private RoleServiceImpl service;

    @Test
    void noPermiteReducirPermisosDelAdministrador() {
        Role admin = new Role();
        admin.setId(1);
        admin.setName("ROLE_ADMIN");
        admin.setEsSistema(true);
        when(roleRepository.findById(1)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> service.saveVistasByRole(1, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("administrador");

        verifyNoInteractions(vistaRepository, rolVistaPermisoRepository);
    }

    @Test
    void noPermiteCrearRolesFueraDelCatalogoDefinido() {
        assertThatThrownBy(() -> service.createRole("FARMACIA", "Rol adicional"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ADMIN, MEDICO y ENFERMERA");
    }
}

package com.medicamentos.service.Impl;

import com.medicamentos.domain.enums.RolUsuario;
import com.medicamentos.domain.model.Usuario;
import com.medicamentos.dto.request.EmpleadoUpdateDTO;
import com.medicamentos.repository.UsuarioRepository;
import com.medicamentos.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpleadoServiceImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @InjectMocks private EmpleadoServiceImpl service;

    @Test
    void impideDesactivarAlUltimoAdministrador() {
        Usuario admin = admin();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByRolAndActivoTrueAndResetTokenIsNull(RolUsuario.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.toggleEstado(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos un administrador activo");

        assertThat(admin.getActivo()).isTrue();
        verify(usuarioRepository, never()).save(admin);
    }

    @Test
    void impideCambiarRolAlUltimoAdministrador() {
        Usuario admin = admin();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByRolAndActivoTrueAndResetTokenIsNull(RolUsuario.ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> service.update(1L,
                new EmpleadoUpdateDTO("Admin", "admin@example.test", "12345678", RolUsuario.MEDICO)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos un administrador activo");

        assertThat(admin.getRol()).isEqualTo(RolUsuario.ADMIN);
        verify(usuarioRepository, never()).save(admin);
    }

    @Test
    void permiteDesactivarUnAdministradorSiOtroPermaneceActivo() {
        Usuario admin = admin();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByRolAndActivoTrueAndResetTokenIsNull(RolUsuario.ADMIN)).thenReturn(2L);
        when(usuarioRepository.save(admin)).thenReturn(admin);

        service.toggleEstado(1L);

        assertThat(admin.getActivo()).isFalse();
    }

    private Usuario admin() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("admin");
        usuario.setNombre("Admin");
        usuario.setEmail("admin@example.test");
        usuario.setRol(RolUsuario.ADMIN);
        usuario.setActivo(true);
        return usuario;
    }
}

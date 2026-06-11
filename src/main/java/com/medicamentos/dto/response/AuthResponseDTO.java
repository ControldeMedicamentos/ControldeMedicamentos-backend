package com.medicamentos.dto.response;

import com.medicamentos.domain.enums.RolUsuario;

import java.util.List;

public record AuthResponseDTO(
        String token,
        Long expiresIn,
        String username,
        RolUsuario rol,
        List<String> permisos,
        boolean mustChangePassword
) {
}

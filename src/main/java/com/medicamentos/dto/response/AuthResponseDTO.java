package com.medicamentos.dto.response;

import com.medicamentos.domain.enums.RolUsuario;

public record AuthResponseDTO(
        String token,
        Long expiresIn,
        String username,
        RolUsuario rol
) {
}

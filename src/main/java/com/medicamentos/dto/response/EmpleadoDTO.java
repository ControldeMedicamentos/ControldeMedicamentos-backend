package com.medicamentos.dto.response;

import java.time.LocalDateTime;

public record EmpleadoDTO(
        Long id,
        String username,
        String nombre,
        String email,
        String rol,
        boolean activo,
        LocalDateTime createdAt
) {}

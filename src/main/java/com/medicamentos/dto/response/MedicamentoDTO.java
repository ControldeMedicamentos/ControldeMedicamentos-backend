package com.medicamentos.dto.response;

import java.time.LocalDateTime;

public record MedicamentoDTO(
        Long id,
        String codigoSismed,
        String codigoSiga,
        String descripcionSismed,
        String presentacionFrasco,
        String descripcionCorta,
        Integer conversion,
        Boolean activo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

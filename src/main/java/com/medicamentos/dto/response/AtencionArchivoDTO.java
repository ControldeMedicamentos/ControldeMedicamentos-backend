package com.medicamentos.dto.response;

import java.time.LocalDateTime;

public record AtencionArchivoDTO(
        Long id,
        Long atencionId,
        String nombreOriginal,
        String tipoContenido,
        Long tamanio,
        LocalDateTime createdAt
) {
}

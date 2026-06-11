package com.medicamentos.dto.response;

import java.time.LocalDateTime;

public record PacienteArchivoDTO(
        Long id,
        Long pacienteId,
        String nombreOriginal,
        String tipoContenido,
        Long tamanio,
        LocalDateTime createdAt
) {
}

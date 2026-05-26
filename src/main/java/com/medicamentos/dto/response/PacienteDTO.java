package com.medicamentos.dto.response;

import com.medicamentos.domain.enums.Sexo;

import java.time.LocalDateTime;

public record PacienteDTO(
        Long id,
        String dni,
        String nombresApellidos,
        Integer edad,
        Sexo sexo,
        String carreraArea,
        String cicloAcademico,
        String telefono,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

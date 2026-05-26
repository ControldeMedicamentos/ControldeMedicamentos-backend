package com.medicamentos.dto.response;

import com.medicamentos.domain.enums.Sexo;
import com.medicamentos.domain.enums.TipoDocumento;

import java.time.LocalDateTime;

public record PacienteDTO(
        Long id,
        TipoDocumento tipoDocumento,
        String nroDocumento,
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

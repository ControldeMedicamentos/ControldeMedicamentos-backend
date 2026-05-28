package com.medicamentos.dto.response;

import com.medicamentos.domain.enums.Sexo;
import com.medicamentos.domain.enums.TipoDocumento;
import com.medicamentos.domain.enums.TipoPaciente;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteDTO(
        Long id,
        TipoPaciente tipoPaciente,
        TipoDocumento tipoDocumento,
        String nroDocumento,
        String nombresApellidos,
        LocalDate fechaNacimiento,
        Sexo sexo,
        String carreraArea,
        String cicloAcademico,
        String telefono,
        Boolean activo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

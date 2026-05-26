package com.medicamentos.dto.request;

import com.medicamentos.domain.enums.Sexo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PacienteCreateDTO(
        @NotBlank @Size(max = 15) String dni,
        @NotBlank @Size(max = 150) String nombresApellidos,
        Integer edad,
        @NotNull Sexo sexo,
        @Size(max = 150) String carreraArea,
        @Size(max = 30) String cicloAcademico,
        @Size(max = 20) String telefono
) {
}

package com.medicamentos.dto.request;

import com.medicamentos.domain.enums.Sexo;
import com.medicamentos.domain.enums.TipoDocumento;
import com.medicamentos.domain.enums.TipoPaciente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PacienteCreateDTO(
        @NotNull TipoPaciente tipoPaciente,
        @NotNull TipoDocumento tipoDocumento,
        @NotBlank @Size(max = 20) String nroDocumento,
        @NotBlank @Size(min = 3, max = 150) String nombresApellidos,
        @Past LocalDate fechaNacimiento,
        @NotNull Sexo sexo,
        @Size(max = 150) String carreraArea,
        @Size(max = 30) String cicloAcademico,
        @Size(max = 15) @Pattern(regexp = "^\\d{9}$|^$", message = "Teléfono debe tener 9 dígitos") String telefono
) {
}

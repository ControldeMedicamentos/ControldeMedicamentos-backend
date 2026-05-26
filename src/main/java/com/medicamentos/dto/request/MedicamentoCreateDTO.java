package com.medicamentos.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicamentoCreateDTO(
        @NotBlank @Size(max = 20) String codigoSismed,
        @Size(max = 30) String codigoSiga,
        @NotBlank @Size(max = 300) String descripcionSismed,
        @Size(max = 80) String presentacionFrasco,
        @Size(max = 120) String descripcionCorta,
        @Min(1) Integer conversion,
        Boolean activo
) {
}

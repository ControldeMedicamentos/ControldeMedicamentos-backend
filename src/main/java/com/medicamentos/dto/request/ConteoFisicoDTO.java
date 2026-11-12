package com.medicamentos.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConteoFisicoDTO(
        @NotBlank String codigoSismed,
        @NotNull @Min(0) Integer stockFisico
) {}

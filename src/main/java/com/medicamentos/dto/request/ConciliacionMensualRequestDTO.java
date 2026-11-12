package com.medicamentos.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ConciliacionMensualRequestDTO(
        @NotEmpty List<@Valid ConteoFisicoDTO> conteos,
        @Size(max = 1000) String observacion
) {}

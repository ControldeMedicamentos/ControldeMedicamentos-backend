package com.medicamentos.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record InventarioCreateDTO(
        @NotNull Long medicamentoId,
        @NotNull @Min(0) Integer stockActual,
        @NotNull @Min(0) Integer stockMinimo,
        @Size(max = 80) String lote,
        LocalDate fechaVencimiento
) {
}

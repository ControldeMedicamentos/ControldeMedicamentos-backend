package com.medicamentos.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record InventarioCreateDTO(
        @NotNull Long medicamentoId,
        @NotNull @Min(1) Integer stockActual,
        @Size(max = 80) String lote,
        @PastOrPresent LocalDate fechaIngreso,
        @FutureOrPresent LocalDate fechaVencimiento
) {
}

package com.medicamentos.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record InventarioDTO(
        Long id,
        Long medicamentoId,
        String nombreMedicamento,
        Integer stockActual,
        Integer stockMinimo,
        String lote,
        LocalDate fechaIngreso,
        LocalDate fechaVencimiento,
        LocalDateTime updatedAt
) {
}

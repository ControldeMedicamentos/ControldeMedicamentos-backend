package com.medicamentos.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record InventarioDTO(
        Long id,
        Long medicamentoId,
        String codigoSismed,
        String descripcionSismed,
        Integer stockActual,
        Integer stockMinimo,
        String lote,
        LocalDate fechaVencimiento,
        LocalDateTime updatedAt
) {
}

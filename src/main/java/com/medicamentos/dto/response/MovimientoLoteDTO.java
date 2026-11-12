package com.medicamentos.dto.response;

import java.time.LocalDate;

public record MovimientoLoteDTO(
        Long inventarioId,
        String lote,
        LocalDate fechaVencimiento,
        Integer cantidad,
        Integer stockAntes,
        Integer stockDespues
) {}

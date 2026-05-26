package com.medicamentos.dto.response;

import java.time.LocalDate;

public record AtencionResumenDTO(
        Long id,
        String pacienteNombre,
        String pacienteNroDocumento,
        LocalDate fechaEvaluacion,
        String motivo,
        int cantidadConsumos
) {
}

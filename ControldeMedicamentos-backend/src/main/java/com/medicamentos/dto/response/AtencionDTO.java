package com.medicamentos.dto.response;

import com.medicamentos.domain.enums.TipoDiagnostico;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AtencionDTO(
        Long id,
        Long pacienteId,
        String pacienteDni,
        String pacienteNombre,
        LocalDate fechaEvaluacion,
        String motivo,
        String antecedentes,
        String inmunizaciones,
        String signosVitales,
        String examenFisico,
        String laboratorio,
        String diagnostico1,
        String cie101,
        TipoDiagnostico tipoDiagnostico1,
        String diagnostico2,
        String cie102,
        TipoDiagnostico tipoDiagnostico2,
        String diagnostico3,
        String cie103,
        TipoDiagnostico tipoDiagnostico3,
        String conclusion,
        String derivacion,
        String observaciones,
        String usuarioRegistro,
        List<ConsumoMedicamentoResponseDTO> consumos,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

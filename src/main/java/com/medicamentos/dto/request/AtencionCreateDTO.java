package com.medicamentos.dto.request;

import com.medicamentos.domain.enums.TipoDiagnostico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record AtencionCreateDTO(
        @NotNull Long pacienteId,
        @NotNull LocalDate fechaEvaluacion,
        @NotBlank @Size(max = 300) String motivo,
        String antecedentes,
        String inmunizaciones,
        String signosVitales,
        String examenFisico,
        String laboratorio,
        @Size(max = 150) String diagnostico1,
        @Size(max = 15) String cie101,
        TipoDiagnostico tipoDiagnostico1,
        @Size(max = 150) String diagnostico2,
        @Size(max = 15) String cie102,
        TipoDiagnostico tipoDiagnostico2,
        @Size(max = 150) String diagnostico3,
        @Size(max = 15) String cie103,
        TipoDiagnostico tipoDiagnostico3,
        @Size(max = 80) String conclusion,
        @Size(max = 120) String derivacion,
        String observaciones,
        @Size(max = 80) String usuarioRegistro,
        @Valid List<ConsumoMedicamentoDTO> consumos
) {
}

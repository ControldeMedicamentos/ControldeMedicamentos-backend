package com.medicamentos.dto.request;

import com.medicamentos.domain.enums.TipoProducto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MedicamentoCreateDTO(
        @NotBlank @Size(max = 200) String nombre,
        @Size(max = 30) String registroSanitario,
        TipoProducto tipoProducto,
        @Size(max = 120) String presentacion,
        @Size(max = 150) String fabricante,
        @Size(max = 80) String paisFabricacion,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal precioUnitario,
        @Min(0) Integer stockMinimo,
        Boolean activo
) {
}

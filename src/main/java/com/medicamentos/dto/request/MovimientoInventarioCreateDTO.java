package com.medicamentos.dto.request;

import com.medicamentos.domain.enums.TipoConsumo;
import com.medicamentos.domain.enums.TipoMovimientoInventario;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MovimientoInventarioCreateDTO(
        @NotNull Long medicamentoId,
        Long atencionId,
        @NotNull TipoMovimientoInventario tipoMovimiento,
        TipoConsumo tipoConsumo,
        @NotNull @Min(1) Integer cantidad,
        @NotBlank @Pattern(regexp = "\\d{6}") String periodo,
        String observacion,
        @NotBlank @Size(max = 80) String usuarioRegistro
) {
}

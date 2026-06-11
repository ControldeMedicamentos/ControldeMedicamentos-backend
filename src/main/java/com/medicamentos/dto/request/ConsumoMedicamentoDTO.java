package com.medicamentos.dto.request;

import com.medicamentos.domain.enums.TipoConsumo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ConsumoMedicamentoDTO(
        @NotNull Long medicamentoId,
        @NotNull @Min(1) Integer cantidadConsumida,
        TipoConsumo tipoConsumo
) {
}

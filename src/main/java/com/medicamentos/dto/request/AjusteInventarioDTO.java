package com.medicamentos.dto.request;

import com.medicamentos.domain.enums.TipoMovimientoInventario;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record AjusteInventarioDTO(
        @NotNull Long inventarioId,
        @NotNull TipoMovimientoInventario tipoAjuste,
        @NotNull @Min(1) Integer cantidad,
        @Size(max = 200) String observacion
) {
    private static final Set<TipoMovimientoInventario> TIPOS_PERMITIDOS = Set.of(
            TipoMovimientoInventario.DEVOLUCION,
            TipoMovimientoInventario.VENCIDO,
            TipoMovimientoInventario.MERMA,
            TipoMovimientoInventario.REINGRESO
    );

    @AssertTrue(message = "El tipo de ajuste debe ser DEVOLUCION, VENCIDO o MERMA")
    public boolean isTipoAjusteValido() {
        return tipoAjuste != null && TIPOS_PERMITIDOS.contains(tipoAjuste);
    }
}

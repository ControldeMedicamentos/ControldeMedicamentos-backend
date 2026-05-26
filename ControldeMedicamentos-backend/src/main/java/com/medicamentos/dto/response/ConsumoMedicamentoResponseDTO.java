package com.medicamentos.dto.response;

import com.medicamentos.domain.enums.TipoConsumo;

import java.time.LocalDateTime;

public record ConsumoMedicamentoResponseDTO(
        Long id,
        Long medicamentoId,
        String codigoSismed,
        String descripcionSismed,
        Integer cantidadConsumida,
        TipoConsumo tipoConsumo,
        Long movimientoInventarioId,
        LocalDateTime createdAt
) {
}

package com.medicamentos.dto.response;

import com.medicamentos.domain.enums.TipoMovimientoInventario;

import java.time.LocalDateTime;

public record MovimientoInventarioDTO(
        Long id,
        Long medicamentoId,
        String nombreMedicamento,
        Long atencionId,
        TipoMovimientoInventario tipoMovimiento,
        Integer cantidad,
        String periodo,
        String observacion,
        String usuarioRegistro,
        LocalDateTime createdAt
) {
}

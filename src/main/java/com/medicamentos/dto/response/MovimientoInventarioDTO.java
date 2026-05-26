package com.medicamentos.dto.response;

import com.medicamentos.domain.enums.TipoConsumo;
import com.medicamentos.domain.enums.TipoMovimientoInventario;

import java.time.LocalDateTime;

public record MovimientoInventarioDTO(
        Long id,
        Long medicamentoId,
        String codigoSismed,
        String descripcionSismed,
        Long atencionId,
        TipoMovimientoInventario tipoMovimiento,
        TipoConsumo tipoConsumo,
        Integer cantidad,
        String periodo,
        String observacion,
        String usuarioRegistro,
        LocalDateTime createdAt
) {
}

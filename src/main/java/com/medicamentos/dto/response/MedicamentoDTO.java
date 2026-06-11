package com.medicamentos.dto.response;

import com.medicamentos.domain.enums.TipoProducto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MedicamentoDTO(
        Long id,
        String nombre,
        String registroSanitario,
        TipoProducto tipoProducto,
        String presentacion,
        String fabricante,
        String paisFabricacion,
        BigDecimal precioUnitario,
        Integer stockMinimo,
        String codigoSismed,
        String descripcionSismed,
        Boolean activo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

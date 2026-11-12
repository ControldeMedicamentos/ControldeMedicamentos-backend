package com.medicamentos.dto.response;

import com.medicamentos.domain.enums.ResultadoConciliacion;

import java.time.LocalDateTime;
import java.util.List;

public record ConciliacionMensualDTO(
        Long id,
        String periodo,
        ResultadoConciliacion resultado,
        String observacion,
        String usuarioRegistro,
        LocalDateTime updatedAt,
        List<ConciliacionDetalleDTO> detalles
) {}

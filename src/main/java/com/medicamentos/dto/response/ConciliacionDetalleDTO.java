package com.medicamentos.dto.response;

public record ConciliacionDetalleDTO(
        String codigoSismed,
        String descripcionSismed,
        Integer stockSistema,
        Integer stockFisico,
        Integer diferencia
) {}

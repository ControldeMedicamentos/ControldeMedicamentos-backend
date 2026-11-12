package com.medicamentos.mapper;

import com.medicamentos.domain.model.MovimientoInventario;
import com.medicamentos.dto.response.MovimientoInventarioDTO;
import com.medicamentos.dto.response.MovimientoLoteDTO;
import org.springframework.stereotype.Component;

@Component
public class MovimientoInventarioMapper {

    public MovimientoInventarioDTO toDTO(MovimientoInventario movimiento) {
        Long atencionId = movimiento.getAtencion() == null ? null : movimiento.getAtencion().getId();
        return new MovimientoInventarioDTO(
                movimiento.getId(),
                movimiento.getMedicamento().getId(),
                movimiento.getMedicamento().getNombre(),
                atencionId,
                movimiento.getTipoMovimiento(),
                movimiento.getCantidad(),
                movimiento.getPeriodo(),
                movimiento.getObservacion(),
                movimiento.getUsuarioRegistro(),
                movimiento.getCreatedAt(),
                movimiento.getLotes().stream()
                        .map(detalle -> new MovimientoLoteDTO(
                                detalle.getInventario().getId(),
                                detalle.getInventario().getLote(),
                                detalle.getInventario().getFechaVencimiento(),
                                detalle.getCantidad(),
                                detalle.getStockAntes(),
                                detalle.getStockDespues()))
                        .toList()
        );
    }
}

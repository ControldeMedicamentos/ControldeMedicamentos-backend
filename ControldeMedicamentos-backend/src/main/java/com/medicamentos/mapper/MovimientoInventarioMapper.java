package com.medicamentos.mapper;

import com.medicamentos.domain.model.MovimientoInventario;
import com.medicamentos.dto.response.MovimientoInventarioDTO;
import org.springframework.stereotype.Component;

@Component
public class MovimientoInventarioMapper {

    public MovimientoInventarioDTO toDTO(MovimientoInventario movimiento) {
        Long atencionId = movimiento.getAtencion() == null ? null : movimiento.getAtencion().getId();
        return new MovimientoInventarioDTO(
                movimiento.getId(),
                movimiento.getMedicamento().getId(),
                movimiento.getMedicamento().getCodigoSismed(),
                movimiento.getMedicamento().getDescripcionSismed(),
                atencionId,
                movimiento.getTipoMovimiento(),
                movimiento.getTipoConsumo(),
                movimiento.getCantidad(),
                movimiento.getPeriodo(),
                movimiento.getObservacion(),
                movimiento.getUsuarioRegistro(),
                movimiento.getCreatedAt()
        );
    }
}

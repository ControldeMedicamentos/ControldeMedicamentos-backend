package com.medicamentos.mapper;

import com.medicamentos.domain.model.Inventario;
import com.medicamentos.dto.response.InventarioDTO;
import org.springframework.stereotype.Component;

@Component
public class InventarioMapper {

    public InventarioDTO toDTO(Inventario inventario) {
        return new InventarioDTO(
                inventario.getId(),
                inventario.getMedicamento().getId(),
                inventario.getMedicamento().getCodigoSismed(),
                inventario.getMedicamento().getDescripcionSismed(),
                inventario.getStockActual(),
                inventario.getStockMinimo(),
                inventario.getLote(),
                inventario.getFechaVencimiento(),
                inventario.getUpdatedAt()
        );
    }
}

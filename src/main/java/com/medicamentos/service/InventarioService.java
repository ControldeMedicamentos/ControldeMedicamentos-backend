package com.medicamentos.service;

import com.medicamentos.domain.enums.TipoConsumo;
import com.medicamentos.domain.model.Atencion;
import com.medicamentos.domain.model.MovimientoInventario;
import com.medicamentos.dto.request.InventarioCreateDTO;
import com.medicamentos.dto.response.InventarioDTO;

import java.util.List;

public interface InventarioService {

    List<InventarioDTO> findAll();

    List<InventarioDTO> findByMedicamento(Long medicamentoId);

    List<InventarioDTO> findLowStock();

    InventarioDTO create(InventarioCreateDTO request);

    InventarioDTO update(Long id, InventarioCreateDTO request);

    MovimientoInventario descontarStock(Long medicamentoId, Integer cantidad);

    MovimientoInventario descontarStock(
            Long medicamentoId,
            Integer cantidad,
            Atencion atencion,
            TipoConsumo tipoConsumo,
            String usuarioRegistro
    );
}

package com.medicamentos.service;

import com.medicamentos.dto.request.InventarioCreateDTO;
import com.medicamentos.dto.response.InventarioDTO;

import java.util.List;

public interface InventarioService {

    List<InventarioDTO> findAll();

    List<InventarioDTO> findByMedicamento(Long medicamentoId);

    InventarioDTO create(InventarioCreateDTO request);
}

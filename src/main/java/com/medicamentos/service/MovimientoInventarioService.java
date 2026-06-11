package com.medicamentos.service;

import com.medicamentos.dto.request.MovimientoInventarioCreateDTO;
import com.medicamentos.dto.response.MovimientoInventarioDTO;

import java.util.List;

public interface MovimientoInventarioService {

    List<MovimientoInventarioDTO> findAll();

    List<MovimientoInventarioDTO> findByPeriodo(String periodo);

    List<MovimientoInventarioDTO> findByMedicamento(Long medicamentoId);

    MovimientoInventarioDTO create(MovimientoInventarioCreateDTO request);
}

package com.medicamentos.service.Impl;

import com.medicamentos.dto.request.MovimientoInventarioCreateDTO;
import com.medicamentos.dto.response.MovimientoInventarioDTO;
import com.medicamentos.mapper.MovimientoInventarioMapper;
import com.medicamentos.repository.MovimientoInventarioRepository;
import com.medicamentos.service.MovimientoInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final MovimientoInventarioMapper movimientoInventarioMapper;

    @Override
    public List<MovimientoInventarioDTO> findAll() {
        return movimientoInventarioRepository.findAll().stream().map(movimientoInventarioMapper::toDTO).toList();
    }

    @Override
    public List<MovimientoInventarioDTO> findByPeriodo(String periodo) {
        return movimientoInventarioRepository.findByPeriodo(periodo).stream().map(movimientoInventarioMapper::toDTO).toList();
    }

    @Override
    public List<MovimientoInventarioDTO> findByMedicamento(Long medicamentoId) {
        return movimientoInventarioRepository.findByMedicamentoIdOrderByCreatedAtDesc(medicamentoId)
                .stream().map(movimientoInventarioMapper::toDTO).toList();
    }

    @Override
    public MovimientoInventarioDTO create(MovimientoInventarioCreateDTO request) {
        // Un movimiento aislado no actualiza el lote y descuadra el stock físico del reporte.
        throw new IllegalArgumentException("Registre ingresos desde lotes, consumos desde atenciones y salidas desde ajustes de inventario.");
    }
}

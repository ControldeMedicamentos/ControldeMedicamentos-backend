package com.medicamentos.service.Impl;

import com.medicamentos.domain.model.Atencion;
import com.medicamentos.domain.model.Medicamento;
import com.medicamentos.domain.model.MovimientoInventario;
import com.medicamentos.dto.request.MovimientoInventarioCreateDTO;
import com.medicamentos.dto.response.MovimientoInventarioDTO;
import com.medicamentos.exception.ResourceNotFoundException;
import com.medicamentos.mapper.MovimientoInventarioMapper;
import com.medicamentos.repository.AtencionRepository;
import com.medicamentos.repository.MedicamentoRepository;
import com.medicamentos.repository.MovimientoInventarioRepository;
import com.medicamentos.service.MovimientoInventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoInventarioServiceImpl implements MovimientoInventarioService {

    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final AtencionRepository atencionRepository;
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
    public MovimientoInventarioDTO create(MovimientoInventarioCreateDTO request) {
        Medicamento medicamento = medicamentoRepository.findById(request.medicamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado: " + request.medicamentoId()));
        Atencion atencion = request.atencionId() == null ? null : atencionRepository.findById(request.atencionId())
                .orElseThrow(() -> new ResourceNotFoundException("Atencion no encontrada: " + request.atencionId()));
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setMedicamento(medicamento);
        movimiento.setAtencion(atencion);
        movimiento.setTipoMovimiento(request.tipoMovimiento());
        movimiento.setTipoConsumo(request.tipoConsumo());
        movimiento.setCantidad(request.cantidad());
        movimiento.setPeriodo(request.periodo());
        movimiento.setObservacion(request.observacion());
        movimiento.setUsuarioRegistro(request.usuarioRegistro());
        return movimientoInventarioMapper.toDTO(movimientoInventarioRepository.save(movimiento));
    }
}

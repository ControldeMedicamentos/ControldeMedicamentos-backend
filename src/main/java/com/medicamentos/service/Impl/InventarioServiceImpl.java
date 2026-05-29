package com.medicamentos.service.Impl;

import com.medicamentos.domain.enums.TipoConsumo;
import com.medicamentos.domain.enums.TipoMovimientoInventario;
import com.medicamentos.dto.request.AjusteInventarioDTO;
import com.medicamentos.domain.model.Atencion;
import com.medicamentos.domain.model.Inventario;
import com.medicamentos.domain.model.Medicamento;
import com.medicamentos.domain.model.MovimientoInventario;
import com.medicamentos.dto.request.InventarioCreateDTO;
import com.medicamentos.dto.response.InventarioDTO;
import com.medicamentos.exception.ResourceNotFoundException;
import com.medicamentos.exception.StockInsuficienteException;
import com.medicamentos.mapper.InventarioMapper;
import com.medicamentos.repository.InventarioRepository;
import com.medicamentos.repository.MedicamentoRepository;
import com.medicamentos.repository.MovimientoInventarioRepository;
import com.medicamentos.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final InventarioMapper inventarioMapper;

    @Override
    public List<InventarioDTO> findAll() {
        return inventarioRepository.findAll().stream().map(inventarioMapper::toDTO).toList();
    }

    @Override
    public List<InventarioDTO> findByMedicamento(Long medicamentoId) {
        return inventarioRepository.findByMedicamentoId(medicamentoId).stream()
                .map(inventarioMapper::toDTO)
                .toList();
    }

    @Override
    public List<InventarioDTO> findLowStock() {
        return inventarioRepository.findLowStock().stream().map(inventarioMapper::toDTO).toList();
    }

    @Override
    public List<InventarioDTO> findVencidosPendientes() {
        return inventarioRepository.findVencidosPendientes(LocalDate.now())
                .stream()
                .map(inventarioMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public InventarioDTO create(InventarioCreateDTO request) {
        Medicamento medicamento = medicamentoRepository.findById(request.medicamentoId())
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado: " + request.medicamentoId()));

        Inventario inventario = new Inventario();
        inventario.setMedicamento(medicamento);
        inventario.setStockActual(request.stockActual());
        inventario.setStockMinimo(medicamento.getStockMinimo() != null ? medicamento.getStockMinimo() : 0);
        inventario.setLote(request.lote());
        inventario.setFechaIngreso(request.fechaIngreso());
        inventario.setFechaVencimiento(request.fechaVencimiento());
        inventarioRepository.save(inventario);

        if (request.stockActual() > 0) {
            registrarMovimientoIngreso(medicamento, request.stockActual(), request.lote());
        }

        return inventarioMapper.toDTO(inventario);
    }

    private void registrarMovimientoIngreso(Medicamento medicamento, Integer cantidad, String lote) {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setMedicamento(medicamento);
        mov.setTipoMovimiento(TipoMovimientoInventario.INGRESO);
        mov.setCantidad(cantidad);
        mov.setPeriodo(YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM")));
        mov.setObservacion(lote != null ? "Ingreso lote: " + lote : "Ingreso de lote");
        mov.setUsuarioRegistro("SISTEMA");
        movimientoInventarioRepository.save(mov);
    }

    @Override
    public InventarioDTO update(Long id, InventarioCreateDTO request) {
        Inventario inventario = inventarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado: " + id));
        inventario.setStockActual(request.stockActual());
        inventario.setLote(request.lote());
        inventario.setFechaIngreso(request.fechaIngreso());
        inventario.setFechaVencimiento(request.fechaVencimiento());
        return inventarioMapper.toDTO(inventarioRepository.save(inventario));
    }

    @Override
    @Transactional
    public void ajustar(AjusteInventarioDTO request, String usuarioRegistro) {
        Inventario inventario = inventarioRepository.findById(request.inventarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado: " + request.inventarioId()));

        if (request.tipoAjuste() == TipoMovimientoInventario.REINGRESO) {
            inventario.setStockActual(inventario.getStockActual() + request.cantidad());
        } else {
            if (inventario.getStockActual() < request.cantidad()) {
                throw new StockInsuficienteException(
                    "Stock insuficiente. Disponible: " + inventario.getStockActual()
                    + ", solicitado: " + request.cantidad()
                );
            }
            inventario.setStockActual(inventario.getStockActual() - request.cantidad());
        }
        inventarioRepository.save(inventario);

        MovimientoInventario mov = new MovimientoInventario();
        mov.setMedicamento(inventario.getMedicamento());
        mov.setTipoMovimiento(request.tipoAjuste());
        mov.setCantidad(request.cantidad());
        mov.setPeriodo(YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM")));
        mov.setObservacion(request.observacion());
        mov.setUsuarioRegistro(usuarioRegistro != null ? usuarioRegistro : "SISTEMA");
        movimientoInventarioRepository.save(mov);
    }

    @Override
    @Transactional
    public MovimientoInventario descontarStock(Long medicamentoId, Integer cantidad) {
        return descontarStock(medicamentoId, cantidad, null, null, "SISTEMA");
    }

    @Override
    @Transactional
    public MovimientoInventario descontarStock(
            Long medicamentoId,
            Integer cantidad,
            Atencion atencion,
            TipoConsumo tipoConsumo,
            String usuarioRegistro
    ) {
        Medicamento medicamento = medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento no encontrado: " + medicamentoId));
        List<Inventario> inventarios = inventarioRepository.findByMedicamentoIdOrderByFechaVencimientoAscIdAsc(medicamentoId);
        int stockDisponible = inventarios.stream().mapToInt(Inventario::getStockActual).sum();
        if (stockDisponible < cantidad) {
            throw new StockInsuficienteException("Stock insuficiente para " + medicamento.getCodigoSismed()
                    + ". Disponible: " + stockDisponible + ", solicitado: " + cantidad);
        }
        descontarDeInventarios(inventarios, cantidad);
        return registrarMovimiento(medicamento, atencion, tipoConsumo, cantidad, usuarioRegistro);
    }

    private void descontarDeInventarios(List<Inventario> inventarios, Integer cantidad) {
        int pendiente = cantidad;
        for (Inventario inventario : inventarios) {
            if (pendiente == 0) {
                break;
            }
            int descuento = Math.min(inventario.getStockActual(), pendiente);
            inventario.setStockActual(inventario.getStockActual() - descuento);
            pendiente -= descuento;
        }
        inventarioRepository.saveAll(inventarios);
    }

    private MovimientoInventario registrarMovimiento(
            Medicamento medicamento,
            Atencion atencion,
            TipoConsumo tipoConsumo,
            Integer cantidad,
            String usuarioRegistro
    ) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setMedicamento(medicamento);
        movimiento.setAtencion(atencion);
        movimiento.setTipoMovimiento(TipoMovimientoInventario.CONSUMO);
        movimiento.setTipoConsumo(tipoConsumo);
        movimiento.setCantidad(cantidad);
        movimiento.setPeriodo(YearMonth.now().format(DateTimeFormatter.ofPattern("yyyyMM")));
        movimiento.setObservacion("Consumo registrado desde atencion");
        movimiento.setUsuarioRegistro(usuarioRegistro == null ? "SISTEMA" : usuarioRegistro);
        return movimientoInventarioRepository.save(movimiento);
    }
}

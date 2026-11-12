package com.medicamentos.service.Impl;

import com.medicamentos.domain.enums.TipoConsumo;
import com.medicamentos.domain.enums.TipoMovimientoInventario;
import com.medicamentos.dto.request.AjusteInventarioDTO;
import com.medicamentos.domain.model.Atencion;
import com.medicamentos.domain.model.Inventario;
import com.medicamentos.domain.model.Medicamento;
import com.medicamentos.domain.model.MovimientoInventario;
import com.medicamentos.domain.model.MovimientoInventarioLote;
import com.medicamentos.dto.request.InventarioCreateDTO;
import com.medicamentos.dto.response.InventarioDTO;
import com.medicamentos.exception.ResourceNotFoundException;
import com.medicamentos.exception.StockInsuficienteException;
import com.medicamentos.mapper.InventarioMapper;
import com.medicamentos.repository.InventarioRepository;
import com.medicamentos.repository.MedicamentoRepository;
import com.medicamentos.repository.MovimientoInventarioRepository;
import com.medicamentos.repository.MovimientoInventarioLoteRepository;
import com.medicamentos.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventarioServiceImpl implements InventarioService {

    private static final ZoneId ZONA_OPERATIVA = ZoneId.of("America/Lima");

    private final InventarioRepository inventarioRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final MovimientoInventarioLoteRepository movimientoInventarioLoteRepository;
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
    public InventarioDTO create(InventarioCreateDTO request, String usuarioRegistro) {
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
            registrarMovimientoIngreso(inventario, request.stockActual(), usuarioRegistro);
        }

        return inventarioMapper.toDTO(inventario);
    }

    private void registrarMovimientoIngreso(Inventario inventario, Integer cantidad, String usuarioRegistro) {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setMedicamento(inventario.getMedicamento());
        mov.setTipoMovimiento(TipoMovimientoInventario.INGRESO);
        mov.setCantidad(cantidad);
        mov.setPeriodo(YearMonth.now(ZONA_OPERATIVA).format(DateTimeFormatter.ofPattern("yyyyMM")));
        mov.setObservacion(inventario.getLote() != null ? "Ingreso de lote registrado" : "Ingreso sin número de lote");
        mov.setUsuarioRegistro(usuarioRegistro != null ? usuarioRegistro : "SISTEMA");
        MovimientoInventario movimiento = movimientoInventarioRepository.save(mov);
        registrarDetalleLote(movimiento, inventario, cantidad, 0, cantidad);
    }

    @Override
    @Transactional
    public InventarioDTO update(Long id, InventarioCreateDTO request) {
        Inventario inventario = inventarioRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado: " + id));
        if (!inventario.getMedicamento().getId().equals(request.medicamentoId())) {
            throw new IllegalArgumentException("No se puede cambiar el medicamento de un lote existente.");
        }
        if (!inventario.getStockActual().equals(request.stockActual())) {
            throw new IllegalArgumentException("El stock solo puede cambiar mediante ingresos, consumos o ajustes de inventario.");
        }
        inventario.setLote(request.lote());
        inventario.setFechaIngreso(request.fechaIngreso());
        inventario.setFechaVencimiento(request.fechaVencimiento());
        return inventarioMapper.toDTO(inventarioRepository.save(inventario));
    }

    @Override
    @Transactional
    public void ajustar(AjusteInventarioDTO request, String usuarioRegistro) {
        Inventario inventario = inventarioRepository.findByIdForUpdate(request.inventarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado: " + request.inventarioId()));

        int stockAntes = inventario.getStockActual();
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
        mov.setPeriodo(YearMonth.now(ZONA_OPERATIVA).format(DateTimeFormatter.ofPattern("yyyyMM")));
        mov.setObservacion(request.observacion());
        mov.setUsuarioRegistro(usuarioRegistro != null ? usuarioRegistro : "SISTEMA");
        MovimientoInventario movimiento = movimientoInventarioRepository.save(mov);
        registrarDetalleLote(movimiento, inventario, request.cantidad(), stockAntes, inventario.getStockActual());
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
        List<DescuentoLote> descuentos = descontarDeInventarios(inventarios, cantidad);
        MovimientoInventario movimiento = registrarMovimiento(medicamento, atencion, tipoConsumo, cantidad, usuarioRegistro);
        descuentos.forEach(descuento -> registrarDetalleLote(movimiento, descuento.inventario(),
                descuento.cantidad(), descuento.stockAntes(), descuento.stockDespues()));
        return movimiento;
    }

    private List<DescuentoLote> descontarDeInventarios(List<Inventario> inventarios, Integer cantidad) {
        int pendiente = cantidad;
        java.util.ArrayList<DescuentoLote> descuentos = new java.util.ArrayList<>();
        for (Inventario inventario : inventarios) {
            if (pendiente == 0) {
                break;
            }
            int stockAntes = inventario.getStockActual();
            int descuento = Math.min(inventario.getStockActual(), pendiente);
            inventario.setStockActual(inventario.getStockActual() - descuento);
            pendiente -= descuento;
            if (descuento > 0) {
                descuentos.add(new DescuentoLote(inventario, descuento, stockAntes, inventario.getStockActual()));
            }
        }
        inventarioRepository.saveAll(inventarios);
        return descuentos;
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
        movimiento.setPeriodo(YearMonth.now(ZONA_OPERATIVA).format(DateTimeFormatter.ofPattern("yyyyMM")));
        movimiento.setObservacion("Consumo registrado desde atencion");
        movimiento.setUsuarioRegistro(usuarioRegistro == null ? "SISTEMA" : usuarioRegistro);
        return movimientoInventarioRepository.save(movimiento);
    }

    private void registrarDetalleLote(MovimientoInventario movimiento, Inventario inventario, Integer cantidad,
                                      Integer stockAntes, Integer stockDespues) {
        MovimientoInventarioLote detalle = new MovimientoInventarioLote();
        detalle.setMovimiento(movimiento);
        detalle.setInventario(inventario);
        detalle.setCantidad(cantidad);
        detalle.setStockAntes(stockAntes);
        detalle.setStockDespues(stockDespues);
        movimientoInventarioLoteRepository.save(detalle);
    }

    private record DescuentoLote(
            Inventario inventario,
            Integer cantidad,
            Integer stockAntes,
            Integer stockDespues
    ) {}
}

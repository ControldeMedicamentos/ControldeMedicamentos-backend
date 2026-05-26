package com.medicamentos.service.Impl;

import com.medicamentos.domain.enums.TipoMovimientoInventario;
import com.medicamentos.domain.model.MovimientoInventario;
import com.medicamentos.dto.response.ReporteSISMEDDTO;
import com.medicamentos.repository.MovimientoInventarioRepository;
import com.medicamentos.service.ReporteSISMEDService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteSISMEDServiceImpl implements ReporteSISMEDService {

    private final MovimientoInventarioRepository movimientoInventarioRepository;

    @Override
    public List<ReporteSISMEDDTO> generarReporteMensual(String periodo) {
        return movimientoInventarioRepository.findByPeriodo(periodo).stream()
                .collect(Collectors.groupingBy(movimiento -> movimiento.getMedicamento().getCodigoSismed()))
                .values()
                .stream()
                .map(movimientos -> buildReporte(periodo, movimientos))
                .toList();
    }

    private ReporteSISMEDDTO buildReporte(String periodo, List<MovimientoInventario> movimientos) {
        MovimientoInventario first = movimientos.get(0);
        Map<TipoMovimientoInventario, Integer> totales = movimientos.stream()
                .collect(Collectors.groupingBy(
                        MovimientoInventario::getTipoMovimiento,
                        Collectors.summingInt(MovimientoInventario::getCantidad)
                ));
        int saldoInicial = total(totales, TipoMovimientoInventario.SALDO_INICIAL);
        int ingresos = total(totales, TipoMovimientoInventario.INGRESO);
        int reingresos = total(totales, TipoMovimientoInventario.REINGRESO);
        int consumos = total(totales, TipoMovimientoInventario.CONSUMO);
        int devoluciones = total(totales, TipoMovimientoInventario.DEVOLUCION);
        int vencidos = total(totales, TipoMovimientoInventario.VENCIDO);
        int merma = total(totales, TipoMovimientoInventario.MERMA);
        int distribucion = total(totales, TipoMovimientoInventario.DISTRIBUCION);
        int transferencia = total(totales, TipoMovimientoInventario.TRANSFERENCIA);
        int stockFinal = saldoInicial + ingresos + reingresos - consumos - devoluciones - vencidos - merma - distribucion - transferencia;
        return new ReporteSISMEDDTO(
                periodo,
                first.getMedicamento().getCodigoSismed(),
                first.getMedicamento().getDescripcionSismed(),
                saldoInicial,
                ingresos,
                reingresos,
                consumos,
                devoluciones,
                vencidos,
                merma,
                distribucion,
                transferencia,
                stockFinal
        );
    }

    private int total(Map<TipoMovimientoInventario, Integer> totales, TipoMovimientoInventario tipo) {
        return totales.getOrDefault(tipo, 0);
    }
}

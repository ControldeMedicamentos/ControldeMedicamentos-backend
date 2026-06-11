package com.medicamentos.service.Impl;

import com.medicamentos.domain.enums.TipoPaciente;
import com.medicamentos.domain.enums.TipoMovimientoInventario;
import com.medicamentos.domain.model.Medicamento;
import com.medicamentos.domain.model.MovimientoInventario;
import com.medicamentos.dto.response.ReporteSISMEDDTO;
import com.medicamentos.exception.DuplicateResourceException;
import com.medicamentos.repository.MovimientoInventarioRepository;
import com.medicamentos.service.ReporteSISMEDService;
import com.medicamentos.util.SISMEDExcelExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteSISMEDServiceImpl implements ReporteSISMEDService {

    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final SISMEDExcelExporter sismedExcelExporter;

    @Override
    @Transactional(readOnly = true)
    public List<ReporteSISMEDDTO> generarReporteMensual(String periodo) {
        return movimientoInventarioRepository.findByPeriodo(periodo).stream()
                .collect(Collectors.groupingBy(m -> m.getMedicamento().getId()))
                .values()
                .stream()
                .map(movimientos -> buildReporte(periodo, movimientos))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarExcel(String periodo) {
        return sismedExcelExporter.exportar(generarReporteMensual(periodo), periodo);
    }

    private ReporteSISMEDDTO buildReporte(String periodo, List<MovimientoInventario> movimientos) {
        MovimientoInventario first = movimientos.get(0);
        Map<TipoMovimientoInventario, Integer> totales = movimientos.stream()
                .collect(Collectors.groupingBy(
                        MovimientoInventario::getTipoMovimiento,
                        Collectors.summingInt(MovimientoInventario::getCantidad)
                ));

        int saldoInicial    = total(totales, TipoMovimientoInventario.SALDO_INICIAL);
        int ingresos        = total(totales, TipoMovimientoInventario.INGRESO);
        int reingresos      = total(totales, TipoMovimientoInventario.REINGRESO);
        int alumno          = totalPorTipoPaciente(movimientos, TipoPaciente.ESTUDIANTE);
        int docente         = totalPorTipoPaciente(movimientos, TipoPaciente.DOCENTE);
        int administrativo  = totalPorTipoPaciente(movimientos, TipoPaciente.ADMINISTRATIVO);
        int invitado        = totalPorTipoPaciente(movimientos, TipoPaciente.INVITADO);
        int consumos        = alumno + docente + administrativo + invitado;
        int devoluciones    = total(totales, TipoMovimientoInventario.DEVOLUCION);
        int vencidos        = total(totales, TipoMovimientoInventario.VENCIDO);
        int merma           = total(totales, TipoMovimientoInventario.MERMA);
        int distribucion    = total(totales, TipoMovimientoInventario.DISTRIBUCION);
        int transferencia   = total(totales, TipoMovimientoInventario.TRANSFERENCIA);
        int stockFinal      = saldoInicial + ingresos + reingresos - consumos
                            - devoluciones - vencidos - merma - distribucion - transferencia;

        return new ReporteSISMEDDTO(
                periodo,
                first.getMedicamento().getNombre(),
                saldoInicial,
                ingresos,
                reingresos,
                alumno,
                docente,
                administrativo,
                invitado,
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

    @Override
    @Transactional(readOnly = true)
    public boolean isPeriodoCerrado(String periodo) {
        YearMonth ym = YearMonth.parse(periodo, DateTimeFormatter.ofPattern("yyyyMM"));
        String nextPeriodo = ym.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
        return movimientoInventarioRepository.existsByPeriodoAndTipoMovimiento(
                nextPeriodo, TipoMovimientoInventario.SALDO_INICIAL);
    }

    @Override
    @Transactional
    public int cerrarMes(String periodo) {
        YearMonth ym = YearMonth.parse(periodo, DateTimeFormatter.ofPattern("yyyyMM"));
        String nextPeriodo = ym.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));

        if (movimientoInventarioRepository.existsByPeriodoAndTipoMovimiento(
                nextPeriodo, TipoMovimientoInventario.SALDO_INICIAL)) {
            throw new DuplicateResourceException("El período " + periodo + " ya fue cerrado.");
        }

        List<MovimientoInventario> todos = movimientoInventarioRepository.findByPeriodo(periodo);

        Map<Long, List<MovimientoInventario>> porMedicamento = todos.stream()
                .collect(Collectors.groupingBy(m -> m.getMedicamento().getId()));

        int count = 0;
        for (Map.Entry<Long, List<MovimientoInventario>> entry : porMedicamento.entrySet()) {
            List<MovimientoInventario> movs = entry.getValue();
            int stockFinal = calcularStockFinal(movs);
            if (stockFinal < 0) stockFinal = 0;

            Medicamento medicamento = movs.get(0).getMedicamento();
            MovimientoInventario saldo = new MovimientoInventario();
            saldo.setMedicamento(medicamento);
            saldo.setTipoMovimiento(TipoMovimientoInventario.SALDO_INICIAL);
            saldo.setCantidad(stockFinal);
            saldo.setPeriodo(nextPeriodo);
            saldo.setObservacion("Saldo inicial · cierre de " + periodo);
            saldo.setUsuarioRegistro("SISTEMA");
            movimientoInventarioRepository.save(saldo);
            count++;
        }
        return count;
    }

    private int calcularStockFinal(List<MovimientoInventario> movimientos) {
        Map<TipoMovimientoInventario, Integer> totales = movimientos.stream()
                .collect(Collectors.groupingBy(
                        MovimientoInventario::getTipoMovimiento,
                        Collectors.summingInt(MovimientoInventario::getCantidad)
                ));
        int saldoInicial = totales.getOrDefault(TipoMovimientoInventario.SALDO_INICIAL, 0);
        int ingresos     = totales.getOrDefault(TipoMovimientoInventario.INGRESO, 0);
        int reingresos   = totales.getOrDefault(TipoMovimientoInventario.REINGRESO, 0);
        int consumos     = movimientos.stream()
                .filter(m -> m.getTipoMovimiento() == TipoMovimientoInventario.CONSUMO)
                .mapToInt(MovimientoInventario::getCantidad).sum();
        int devoluciones = totales.getOrDefault(TipoMovimientoInventario.DEVOLUCION, 0);
        int vencidos     = totales.getOrDefault(TipoMovimientoInventario.VENCIDO, 0);
        int merma        = totales.getOrDefault(TipoMovimientoInventario.MERMA, 0);
        int distribucion = totales.getOrDefault(TipoMovimientoInventario.DISTRIBUCION, 0);
        int transferencia= totales.getOrDefault(TipoMovimientoInventario.TRANSFERENCIA, 0);
        return saldoInicial + ingresos + reingresos - consumos - devoluciones - vencidos - merma - distribucion - transferencia;
    }

    private int totalPorTipoPaciente(List<MovimientoInventario> movimientos, TipoPaciente tipoPaciente) {
        return movimientos.stream()
                .filter(m -> m.getTipoMovimiento() == TipoMovimientoInventario.CONSUMO)
                .filter(m -> m.getAtencion() != null)
                .filter(m -> m.getAtencion().getPaciente() != null)
                .filter(m -> tipoPaciente == m.getAtencion().getPaciente().getTipoPaciente())
                .mapToInt(MovimientoInventario::getCantidad)
                .sum();
    }
}

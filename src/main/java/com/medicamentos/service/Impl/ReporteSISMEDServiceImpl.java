package com.medicamentos.service.Impl;

import com.medicamentos.domain.enums.TipoConsumo;
import com.medicamentos.domain.enums.TipoMovimientoInventario;
import com.medicamentos.domain.enums.ResultadoConciliacion;
import com.medicamentos.domain.model.ConciliacionMensual;
import com.medicamentos.domain.model.ConciliacionMensualDetalle;
import com.medicamentos.domain.model.Medicamento;
import com.medicamentos.domain.model.MovimientoInventario;
import com.medicamentos.dto.request.ConciliacionMensualRequestDTO;
import com.medicamentos.dto.response.ConciliacionDetalleDTO;
import com.medicamentos.dto.response.ConciliacionMensualDTO;
import com.medicamentos.dto.response.ReporteSISMEDDTO;
import com.medicamentos.exception.DuplicateResourceException;
import com.medicamentos.exception.ResourceNotFoundException;
import com.medicamentos.repository.ConciliacionMensualRepository;
import com.medicamentos.repository.MedicamentoRepository;
import com.medicamentos.repository.MovimientoInventarioRepository;
import com.medicamentos.service.ReporteSISMEDService;
import com.medicamentos.util.SISMEDExcelExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReporteSISMEDServiceImpl implements ReporteSISMEDService {

    private static final ZoneId ZONA_OPERATIVA = ZoneId.of("America/Lima");

    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final SISMEDExcelExporter sismedExcelExporter;
    private final ConciliacionMensualRepository conciliacionMensualRepository;
    private final MedicamentoRepository medicamentoRepository;

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
        int venta           = totalPorTipoConsumo(movimientos, TipoConsumo.VENTA);
        int sis             = totalPorTipoConsumo(movimientos, TipoConsumo.SIS);
        int intersanidad    = totalPorTipoConsumo(movimientos, TipoConsumo.INTERSANIDAD);
        int factoresPerdida = totalPorTipoConsumo(movimientos, TipoConsumo.FACTORES_PERDIDA);
        int defuncion       = totalPorTipoConsumo(movimientos, TipoConsumo.DEFUNCION_NACIMIENTO);
        int exonerado       = totalPorTipoConsumo(movimientos, TipoConsumo.EXONERADO);
        int soat            = totalPorTipoConsumo(movimientos, TipoConsumo.SOAT);
        int credito         = totalPorTipoConsumo(movimientos, TipoConsumo.CREDITO_HOSPITALARIO);
        int otroConvenio    = totalPorTipoConsumo(movimientos, TipoConsumo.OTRO_CONVENIO);
        int consumos        = total(totales, TipoMovimientoInventario.CONSUMO);
        int devoluciones    = total(totales, TipoMovimientoInventario.DEVOLUCION);
        int vencidos        = total(totales, TipoMovimientoInventario.VENCIDO);
        int merma           = total(totales, TipoMovimientoInventario.MERMA);
        int distribucion    = total(totales, TipoMovimientoInventario.DISTRIBUCION);
        int transferencia   = total(totales, TipoMovimientoInventario.TRANSFERENCIA);
        int stockFinal      = saldoInicial + ingresos + reingresos - consumos
                            - devoluciones - vencidos - merma - distribucion - transferencia;

        return new ReporteSISMEDDTO(
                periodo,
                first.getMedicamento().getCodigoSismed(),
                first.getMedicamento().getDescripcionSismed() != null
                        ? first.getMedicamento().getDescripcionSismed()
                        : first.getMedicamento().getNombre(),
                first.getMedicamento().getPrecioUnitario(),
                saldoInicial,
                ingresos,
                reingresos,
                venta,
                sis,
                intersanidad,
                factoresPerdida,
                defuncion,
                exonerado,
                soat,
                credito,
                otroConvenio,
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
    @Transactional(readOnly = true)
    public ConciliacionMensualDTO obtenerConciliacion(String periodo) {
        return conciliacionMensualRepository.findByPeriodo(periodo)
                .map(this::toConciliacionDTO)
                .orElseThrow(() -> new ResourceNotFoundException("No existe conciliación para el período " + periodo + "."));
    }

    @Override
    @Transactional
    public ConciliacionMensualDTO conciliar(String periodo, ConciliacionMensualRequestDTO request,
                                             String usuarioRegistro) {
        YearMonth ym = parsePeriodo(periodo);
        if (ym.isAfter(YearMonth.now(ZONA_OPERATIVA))) {
            throw new IllegalArgumentException("No se puede conciliar un período futuro.");
        }
        if (isPeriodoCerrado(periodo)) {
            throw new IllegalArgumentException("El período " + periodo + " ya está cerrado y no puede modificarse.");
        }

        List<ReporteSISMEDDTO> reportes = generarReporteMensual(periodo);
        if (reportes.isEmpty()) {
            throw new IllegalArgumentException("No hay movimientos para conciliar el período " + periodo + ".");
        }
        reportes.forEach(reporte -> {
            if (reporte.stockFinal() < 0) {
                throw new IllegalArgumentException(
                        "Stock final negativo para " + reporte.descripcionSismed() + ". Revise los movimientos.");
            }
            int consumosClasificados = reporte.venta() + reporte.sis() + reporte.intersanidad()
                    + reporte.factoresPerdida() + reporte.defuncionNacimiento() + reporte.exonerado()
                    + reporte.soat() + reporte.creditoHospitalario() + reporte.otroConvenio();
            if (reporte.consumos() != consumosClasificados) {
                throw new IllegalArgumentException(
                        "Los consumos por tipo SISMED no cuadran para " + reporte.descripcionSismed() + ".");
            }
        });

        Map<String, Integer> conteos = request.conteos().stream()
                .collect(Collectors.toMap(
                        conteo -> conteo.codigoSismed().trim(),
                        conteo -> conteo.stockFisico(),
                        (primero, repetido) -> {
                            throw new IllegalArgumentException("Código SISMED repetido en el conteo físico.");
                        }));
        HashSet<String> codigosReporte = reportes.stream()
                .map(ReporteSISMEDDTO::codigoSismed)
                .collect(Collectors.toCollection(HashSet::new));
        if (!conteos.keySet().equals(codigosReporte)) {
            throw new IllegalArgumentException("El conteo físico debe incluir exactamente todos los medicamentos del reporte.");
        }

        List<ConciliacionMensualDetalle> detalles = reportes.stream().map(reporte -> {
            Medicamento medicamento = medicamentoRepository.findByCodigoSismed(reporte.codigoSismed())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Medicamento SISMED no encontrado: " + reporte.codigoSismed()));
            int stockFisico = conteos.get(reporte.codigoSismed());
            ConciliacionMensualDetalle detalle = new ConciliacionMensualDetalle();
            detalle.setMedicamento(medicamento);
            detalle.setStockSistema(reporte.stockFinal());
            detalle.setStockFisico(stockFisico);
            detalle.setDiferencia(stockFisico - reporte.stockFinal());
            return detalle;
        }).toList();

        ConciliacionMensual conciliacion = conciliacionMensualRepository.findByPeriodo(periodo)
                .orElseGet(ConciliacionMensual::new);
        conciliacion.setPeriodo(periodo);
        conciliacion.setResultado(detalles.stream().allMatch(detalle -> detalle.getDiferencia() == 0)
                ? ResultadoConciliacion.CUADRADO
                : ResultadoConciliacion.CON_DIFERENCIAS);
        conciliacion.setObservacion(request.observacion());
        conciliacion.setUsuarioRegistro(usuarioRegistro != null ? usuarioRegistro : "SISTEMA");
        conciliacion.reemplazarDetalles(detalles);
        return toConciliacionDTO(conciliacionMensualRepository.save(conciliacion));
    }

    @Override
    @Transactional
    public int cerrarMes(String periodo) {
        YearMonth ym = parsePeriodo(periodo);
        if (!ym.isBefore(YearMonth.now(ZONA_OPERATIVA))) {
            throw new IllegalArgumentException("Solo se pueden cerrar meses anteriores al período actual.");
        }
        String nextPeriodo = ym.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));

        if (movimientoInventarioRepository.existsByPeriodoAndTipoMovimiento(
                nextPeriodo, TipoMovimientoInventario.SALDO_INICIAL)) {
            throw new DuplicateResourceException("El período " + periodo + " ya fue cerrado.");
        }

        List<MovimientoInventario> todos = movimientoInventarioRepository.findByPeriodo(periodo);
        if (todos.isEmpty()) {
            throw new IllegalArgumentException("No hay movimientos para cerrar el período " + periodo + ".");
        }

        Map<Long, List<MovimientoInventario>> porMedicamento = todos.stream()
                .collect(Collectors.groupingBy(m -> m.getMedicamento().getId()));
        List<List<MovimientoInventario>> grupos = porMedicamento.values().stream().toList();
        List<ReporteSISMEDDTO> reportes = grupos.stream()
                .map(movs -> buildReporte(periodo, movs))
                .toList();

        for (int index = 0; index < grupos.size(); index++) {
            List<MovimientoInventario> movs = grupos.get(index);
            ReporteSISMEDDTO reporte = reportes.get(index);
            if (movs.stream().anyMatch(m -> m.getTipoMovimiento() == TipoMovimientoInventario.AJUSTE)) {
                throw new IllegalArgumentException("Hay movimientos AJUSTE sin clasificación SISMED para " + reporte.descripcionSismed() + ".");
            }
            int consumosClasificados = reporte.venta() + reporte.sis() + reporte.intersanidad()
                    + reporte.factoresPerdida() + reporte.defuncionNacimiento() + reporte.exonerado()
                    + reporte.soat() + reporte.creditoHospitalario() + reporte.otroConvenio();
            if (reporte.consumos() != consumosClasificados) {
                throw new IllegalArgumentException("Los consumos por tipo SISMED no cuadran para " + reporte.descripcionSismed() + ".");
            }
            int stockFinal = reporte.stockFinal();
            if (stockFinal < 0) {
                throw new IllegalArgumentException("Stock final negativo para " + reporte.descripcionSismed() + ": " + stockFinal + ". Revise los movimientos.");
            }
        }

        ConciliacionMensual conciliacion = conciliacionMensualRepository.findByPeriodo(periodo)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Debe registrar la conciliación física antes de cerrar el período."));
        Map<String, ConciliacionMensualDetalle> conciliados = conciliacion.getDetalles().stream()
                .collect(Collectors.toMap(
                        detalle -> detalle.getMedicamento().getCodigoSismed(),
                        Function.identity()));

        for (ReporteSISMEDDTO reporte : reportes) {
            ConciliacionMensualDetalle detalleConciliado = conciliados.get(reporte.codigoSismed());
            if (detalleConciliado == null
                    || !detalleConciliado.getStockSistema().equals(reporte.stockFinal())
                    || detalleConciliado.getDiferencia() != 0) {
                throw new IllegalArgumentException(
                        "La conciliación física tiene diferencias o quedó desactualizada para "
                                + reporte.descripcionSismed() + ".");
            }
        }

        int count = 0;
        for (int index = 0; index < grupos.size(); index++) {
            List<MovimientoInventario> movs = grupos.get(index);
            ReporteSISMEDDTO reporte = reportes.get(index);
            Medicamento medicamento = movs.get(0).getMedicamento();
            MovimientoInventario saldo = new MovimientoInventario();
            saldo.setMedicamento(medicamento);
            saldo.setTipoMovimiento(TipoMovimientoInventario.SALDO_INICIAL);
            saldo.setCantidad(reporte.stockFinal());
            saldo.setPeriodo(nextPeriodo);
            saldo.setObservacion("Saldo inicial · cierre de " + periodo);
            saldo.setUsuarioRegistro("SISTEMA");
            movimientoInventarioRepository.save(saldo);
            count++;
        }
        return count;
    }

    private YearMonth parsePeriodo(String periodo) {
        try {
            return YearMonth.parse(periodo, DateTimeFormatter.ofPattern("yyyyMM"));
        } catch (Exception exception) {
            throw new IllegalArgumentException("El período debe tener formato yyyyMM.");
        }
    }

    private ConciliacionMensualDTO toConciliacionDTO(ConciliacionMensual conciliacion) {
        List<ConciliacionDetalleDTO> detalles = conciliacion.getDetalles().stream()
                .map(detalle -> new ConciliacionDetalleDTO(
                        detalle.getMedicamento().getCodigoSismed(),
                        detalle.getMedicamento().getDescripcionSismed() != null
                                ? detalle.getMedicamento().getDescripcionSismed()
                                : detalle.getMedicamento().getNombre(),
                        detalle.getStockSistema(),
                        detalle.getStockFisico(),
                        detalle.getDiferencia()))
                .sorted(java.util.Comparator.comparing(ConciliacionDetalleDTO::codigoSismed))
                .toList();
        return new ConciliacionMensualDTO(
                conciliacion.getId(), conciliacion.getPeriodo(), conciliacion.getResultado(),
                conciliacion.getObservacion(), conciliacion.getUsuarioRegistro(),
                conciliacion.getUpdatedAt(), detalles);
    }

    private int totalPorTipoConsumo(List<MovimientoInventario> movimientos, TipoConsumo tipoConsumo) {
        return movimientos.stream()
                .filter(m -> m.getTipoMovimiento() == TipoMovimientoInventario.CONSUMO)
                .filter(m -> tipoConsumo == m.getTipoConsumo())
                .mapToInt(MovimientoInventario::getCantidad)
                .sum();
    }
}

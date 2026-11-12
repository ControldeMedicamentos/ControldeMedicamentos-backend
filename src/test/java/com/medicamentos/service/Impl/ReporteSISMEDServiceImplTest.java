package com.medicamentos.service.Impl;

import com.medicamentos.domain.enums.TipoConsumo;
import com.medicamentos.domain.enums.TipoMovimientoInventario;
import com.medicamentos.domain.model.Medicamento;
import com.medicamentos.domain.model.MovimientoInventario;
import com.medicamentos.domain.model.ConciliacionMensual;
import com.medicamentos.domain.model.ConciliacionMensualDetalle;
import com.medicamentos.domain.enums.ResultadoConciliacion;
import com.medicamentos.repository.ConciliacionMensualRepository;
import com.medicamentos.repository.MedicamentoRepository;
import com.medicamentos.dto.request.ConciliacionMensualRequestDTO;
import com.medicamentos.dto.request.ConteoFisicoDTO;
import com.medicamentos.repository.MovimientoInventarioRepository;
import com.medicamentos.util.SISMEDExcelExporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReporteSISMEDServiceImplTest {

    @Mock private MovimientoInventarioRepository movimientoRepository;
    @Mock private ConciliacionMensualRepository conciliacionRepository;
    @Mock private MedicamentoRepository medicamentoRepository;
    private ReporteSISMEDServiceImpl service() {
        return new ReporteSISMEDServiceImpl(
                movimientoRepository, new SISMEDExcelExporter(), conciliacionRepository, medicamentoRepository);
    }

    @Test
    void noCierraMesActual() {
        String periodo = YearMonth.now(ZoneId.of("America/Lima"))
                .format(DateTimeFormatter.ofPattern("yyyyMM"));

        assertThatThrownBy(() -> service().cerrarMes(periodo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("meses anteriores");
        verifyNoInteractions(movimientoRepository);
    }

    @Test
    void noCierraMesSinMovimientos() {
        String periodo = periodoAnterior();
        when(movimientoRepository.findByPeriodo(periodo)).thenReturn(List.of());

        assertThatThrownBy(() -> service().cerrarMes(periodo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No hay movimientos");
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void noConvierteStockNegativoEnCero() {
        String periodo = periodoAnterior();
        when(movimientoRepository.findByPeriodo(periodo))
                .thenReturn(List.of(movimiento(TipoMovimientoInventario.MERMA, 2, periodo)));

        assertThatThrownBy(() -> service().cerrarMes(periodo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Stock final negativo");
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void noCierraConConsumosSinClasificacionSismed() {
        String periodo = periodoAnterior();
        when(movimientoRepository.findByPeriodo(periodo)).thenReturn(List.of(
                movimiento(TipoMovimientoInventario.INGRESO, 5, periodo),
                movimiento(TipoMovimientoInventario.CONSUMO, 2, periodo)));

        assertThatThrownBy(() -> service().cerrarMes(periodo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no cuadran");
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void reporteNoOcultaConsumosSinClasificacion() {
        String periodo = periodoAnterior();
        when(movimientoRepository.findByPeriodo(periodo)).thenReturn(List.of(
                movimiento(TipoMovimientoInventario.INGRESO, 5, periodo),
                movimiento(TipoMovimientoInventario.CONSUMO, 2, periodo)));

        var reporte = service().generarReporteMensual(periodo).get(0);

        assertThat(reporte.consumos()).isEqualTo(2);
        assertThat(reporte.stockFinal()).isEqualTo(3);
        assertThat(reporte.venta() + reporte.sis() + reporte.intersanidad()
                + reporte.factoresPerdida() + reporte.defuncionNacimiento()
                + reporte.exonerado() + reporte.soat() + reporte.creditoHospitalario()
                + reporte.otroConvenio()).isZero();
    }

    @Test
    void clasificaConsumosPorTipoSismed() {
        String periodo = periodoAnterior();
        MovimientoInventario consumoSis = movimiento(TipoMovimientoInventario.CONSUMO, 2, periodo);
        consumoSis.setTipoConsumo(TipoConsumo.SIS);
        MovimientoInventario consumoSoat = movimiento(TipoMovimientoInventario.CONSUMO, 1, periodo);
        consumoSoat.setTipoConsumo(TipoConsumo.SOAT);
        when(movimientoRepository.findByPeriodo(periodo)).thenReturn(List.of(
                movimiento(TipoMovimientoInventario.INGRESO, 5, periodo), consumoSis, consumoSoat));

        var reporte = service().generarReporteMensual(periodo).get(0);

        assertThat(reporte.sis()).isEqualTo(2);
        assertThat(reporte.soat()).isEqualTo(1);
        assertThat(reporte.consumos()).isEqualTo(3);
        assertThat(reporte.stockFinal()).isEqualTo(2);
    }

    @Test
    void noCierraConAjustesSinClasificacion() {
        String periodo = periodoAnterior();
        when(movimientoRepository.findByPeriodo(periodo))
                .thenReturn(List.of(movimiento(TipoMovimientoInventario.AJUSTE, 1, periodo)));

        assertThatThrownBy(() -> service().cerrarMes(periodo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("AJUSTE sin clasificación");
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void cierraMesValidoConSaldoExacto() {
        String periodo = periodoAnterior();
        when(movimientoRepository.findByPeriodo(periodo)).thenReturn(List.of(
                movimiento(TipoMovimientoInventario.INGRESO, 7, periodo),
                movimiento(TipoMovimientoInventario.MERMA, 2, periodo)));
        when(conciliacionRepository.findByPeriodo(periodo))
                .thenReturn(Optional.of(conciliacionCuadrada(5)));

        assertThat(service().cerrarMes(periodo)).isEqualTo(1);
        verify(movimientoRepository).save(org.mockito.ArgumentMatchers.argThat(saldo ->
                saldo.getTipoMovimiento() == TipoMovimientoInventario.SALDO_INICIAL
                        && saldo.getCantidad() == 5
                        && saldo.getPeriodo().equals(YearMonth.parse(periodo, DateTimeFormatter.ofPattern("yyyyMM"))
                                .plusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM")))));
    }

    @Test
    void noCierraSinConciliacionFisica() {
        String periodo = periodoAnterior();
        when(movimientoRepository.findByPeriodo(periodo))
                .thenReturn(List.of(movimiento(TipoMovimientoInventario.INGRESO, 5, periodo)));
        when(conciliacionRepository.findByPeriodo(periodo)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().cerrarMes(periodo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("conciliación física");
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void conciliacionComparaStockTeoricoConConteoFisico() {
        String periodo = periodoAnterior();
        Medicamento medicamento = medicamento();
        when(movimientoRepository.findByPeriodo(periodo))
                .thenReturn(List.of(movimiento(TipoMovimientoInventario.INGRESO, 5, periodo)));
        when(medicamentoRepository.findByCodigoSismed("01234")).thenReturn(Optional.of(medicamento));
        when(conciliacionRepository.findByPeriodo(periodo)).thenReturn(Optional.empty());
        when(conciliacionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service().conciliar(periodo,
                new ConciliacionMensualRequestDTO(List.of(new ConteoFisicoDTO("01234", 3)), "Conteo real"),
                "admin");

        assertThat(result.resultado()).isEqualTo(ResultadoConciliacion.CON_DIFERENCIAS);
        assertThat(result.detalles()).singleElement().satisfies(detalle -> {
            assertThat(detalle.stockSistema()).isEqualTo(5);
            assertThat(detalle.stockFisico()).isEqualTo(3);
            assertThat(detalle.diferencia()).isEqualTo(-2);
        });
    }

    private ConciliacionMensual conciliacionCuadrada(int stock) {
        Medicamento medicamento = medicamento();
        ConciliacionMensualDetalle detalle = new ConciliacionMensualDetalle();
        detalle.setMedicamento(medicamento);
        detalle.setStockSistema(stock);
        detalle.setStockFisico(stock);
        detalle.setDiferencia(0);
        ConciliacionMensual conciliacion = new ConciliacionMensual();
        conciliacion.setResultado(ResultadoConciliacion.CUADRADO);
        conciliacion.reemplazarDetalles(List.of(detalle));
        return conciliacion;
    }

    private String periodoAnterior() {
        return YearMonth.now(ZoneId.of("America/Lima")).minusMonths(1)
                .format(DateTimeFormatter.ofPattern("yyyyMM"));
    }

    private MovimientoInventario movimiento(TipoMovimientoInventario tipo, int cantidad, String periodo) {
        Medicamento medicamento = medicamento();
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setMedicamento(medicamento);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setPeriodo(periodo);
        return movimiento;
    }

    private Medicamento medicamento() {
        Medicamento medicamento = new Medicamento();
        medicamento.setId(1L);
        medicamento.setNombre("Prueba");
        medicamento.setCodigoSismed("01234");
        medicamento.setDescripcionSismed("Medicamento de prueba");
        return medicamento;
    }
}

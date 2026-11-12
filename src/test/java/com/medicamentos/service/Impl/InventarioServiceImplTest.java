package com.medicamentos.service.Impl;

import com.medicamentos.domain.model.Inventario;
import com.medicamentos.domain.model.Medicamento;
import com.medicamentos.domain.model.MovimientoInventarioLote;
import com.medicamentos.domain.enums.TipoConsumo;
import com.medicamentos.dto.request.InventarioCreateDTO;
import com.medicamentos.mapper.InventarioMapper;
import com.medicamentos.repository.InventarioRepository;
import com.medicamentos.repository.MedicamentoRepository;
import com.medicamentos.repository.MovimientoInventarioRepository;
import com.medicamentos.repository.MovimientoInventarioLoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {

    @Mock private InventarioRepository inventarioRepository;
    @Mock private MedicamentoRepository medicamentoRepository;
    @Mock private MovimientoInventarioRepository movimientoRepository;
    @Mock private MovimientoInventarioLoteRepository movimientoLoteRepository;

    @Test
    void noPermiteCambiarStockSinMovimiento() {
        Medicamento medicamento = new Medicamento();
        medicamento.setId(2L);
        Inventario lote = new Inventario();
        lote.setMedicamento(medicamento);
        lote.setStockActual(10);
        when(inventarioRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(lote));
        InventarioServiceImpl service = new InventarioServiceImpl(
                inventarioRepository, medicamentoRepository, movimientoRepository,
                movimientoLoteRepository, new InventarioMapper());

        assertThatThrownBy(() -> service.update(3L, new InventarioCreateDTO(2L, 9, "L1", null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El stock solo puede cambiar");
        verify(inventarioRepository, never()).save(any());
    }

    @Test
    void registraCadaLoteConsumidoConStockAntesYDespues() {
        Medicamento medicamento = new Medicamento();
        medicamento.setId(2L);
        medicamento.setCodigoSismed("M001");
        Inventario lote1 = lote(10L, medicamento, "L1", 2);
        Inventario lote2 = lote(11L, medicamento, "L2", 5);
        when(medicamentoRepository.findById(2L)).thenReturn(Optional.of(medicamento));
        when(inventarioRepository.findByMedicamentoIdOrderByFechaVencimientoAscIdAsc(2L))
                .thenReturn(List.of(lote1, lote2));
        when(movimientoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        InventarioServiceImpl service = new InventarioServiceImpl(
                inventarioRepository, medicamentoRepository, movimientoRepository,
                movimientoLoteRepository, new InventarioMapper());

        service.descontarStock(2L, 4, null, TipoConsumo.SIS, "medico");

        var captor = org.mockito.ArgumentCaptor.forClass(MovimientoInventarioLote.class);
        verify(movimientoLoteRepository, times(2)).save(captor.capture());
        assertThat(lote1.getStockActual()).isZero();
        assertThat(lote2.getStockActual()).isEqualTo(3);
        assertThat(captor.getAllValues())
                .extracting(MovimientoInventarioLote::getCantidad,
                        MovimientoInventarioLote::getStockAntes,
                        MovimientoInventarioLote::getStockDespues)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(2, 2, 0),
                        org.assertj.core.groups.Tuple.tuple(2, 5, 3));
    }

    private Inventario lote(Long id, Medicamento medicamento, String numero, int stock) {
        Inventario inventario = new Inventario();
        inventario.setId(id);
        inventario.setMedicamento(medicamento);
        inventario.setLote(numero);
        inventario.setStockActual(stock);
        return inventario;
    }
}

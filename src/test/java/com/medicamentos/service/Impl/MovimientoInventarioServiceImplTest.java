package com.medicamentos.service.Impl;

import com.medicamentos.domain.enums.TipoMovimientoInventario;
import com.medicamentos.dto.request.MovimientoInventarioCreateDTO;
import com.medicamentos.mapper.MovimientoInventarioMapper;
import com.medicamentos.repository.MovimientoInventarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MovimientoInventarioServiceImplTest {

    @Mock private MovimientoInventarioRepository movimientoRepository;

    @Test
    void noRegistraMovimientosSinActualizarLotes() {
        MovimientoInventarioServiceImpl service = new MovimientoInventarioServiceImpl(
                movimientoRepository, new MovimientoInventarioMapper());
        MovimientoInventarioCreateDTO request = new MovimientoInventarioCreateDTO(
                1L, null, TipoMovimientoInventario.INGRESO, null, 5, "202608", null, "usuario");

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Registre ingresos desde lotes");
        verifyNoInteractions(movimientoRepository);
    }
}

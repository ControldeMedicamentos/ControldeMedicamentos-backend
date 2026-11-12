package com.medicamentos.dto.request;

import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsumoMedicamentoDTOValidationTest {

    @Test
    void rechazaConsumoSinClasificacionSismed() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var violations = factory.getValidator().validate(new ConsumoMedicamentoDTO(1L, 1, null));

            assertThat(violations)
                    .extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactly("tipoConsumo");
        }
    }
}

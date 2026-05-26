package com.medicamentos.repository;

import com.medicamentos.domain.model.ConsumoMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsumoMedicamentoRepository extends JpaRepository<ConsumoMedicamento, Long> {

    List<ConsumoMedicamento> findByAtencionId(Long atencionId);
}

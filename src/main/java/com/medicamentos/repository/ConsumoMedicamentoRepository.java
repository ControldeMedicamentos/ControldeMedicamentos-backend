package com.medicamentos.repository;

import com.medicamentos.domain.model.ConsumoMedicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ConsumoMedicamentoRepository extends JpaRepository<ConsumoMedicamento, Long> {

    List<ConsumoMedicamento> findByAtencionId(Long atencionId);

    boolean existsByMedicamentoId(Long medicamentoId);

    @Query("SELECT m.nombre, SUM(c.cantidadConsumida) FROM ConsumoMedicamento c JOIN c.medicamento m JOIN c.atencion a WHERE a.fechaEvaluacion BETWEEN :desde AND :hasta GROUP BY m.id, m.nombre ORDER BY SUM(c.cantidadConsumida) DESC")
    List<Object[]> findTopConsumosRaw(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}

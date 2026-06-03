package com.medicamentos.repository;

import com.medicamentos.domain.enums.TipoMovimientoInventario;
import com.medicamentos.domain.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    @Query("SELECT m FROM MovimientoInventario m " +
           "LEFT JOIN FETCH m.atencion a " +
           "LEFT JOIN FETCH a.paciente " +
           "WHERE m.periodo = :periodo")
    List<MovimientoInventario> findByPeriodo(@Param("periodo") String periodo);

    List<MovimientoInventario> findByPeriodoAndTipoMovimiento(String periodo, TipoMovimientoInventario tipoMovimiento);

    boolean existsByPeriodoAndTipoMovimiento(String periodo, TipoMovimientoInventario tipoMovimiento);

    List<MovimientoInventario> findByMedicamentoCodigoSismedAndPeriodo(String codigoSismed, String periodo);

    List<MovimientoInventario> findByMedicamentoIdOrderByCreatedAtDesc(Long medicamentoId);
}

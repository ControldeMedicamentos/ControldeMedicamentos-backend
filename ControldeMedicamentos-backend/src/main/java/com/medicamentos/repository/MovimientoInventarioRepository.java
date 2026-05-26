package com.medicamentos.repository;

import com.medicamentos.domain.model.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByPeriodo(String periodo);

    List<MovimientoInventario> findByMedicamentoCodigoSismedAndPeriodo(String codigoSismed, String periodo);
}

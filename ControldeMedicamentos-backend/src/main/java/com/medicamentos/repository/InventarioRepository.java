package com.medicamentos.repository;

import com.medicamentos.domain.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    List<Inventario> findByMedicamentoId(Long medicamentoId);

    List<Inventario> findByMedicamentoCodigoSismed(String codigoSismed);
}

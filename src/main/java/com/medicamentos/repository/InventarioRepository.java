package com.medicamentos.repository;

import com.medicamentos.domain.model.Inventario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    List<Inventario> findByMedicamentoId(Long medicamentoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventario> findByMedicamentoIdOrderByFechaVencimientoAscIdAsc(Long medicamentoId);

    List<Inventario> findByMedicamentoCodigoSismed(String codigoSismed);

    @Query("select i from Inventario i where i.stockActual <= i.stockMinimo")
    List<Inventario> findLowStock();
}

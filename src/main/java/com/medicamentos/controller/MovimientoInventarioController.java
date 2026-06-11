package com.medicamentos.controller;

import com.medicamentos.dto.request.MovimientoInventarioCreateDTO;
import com.medicamentos.dto.response.MovimientoInventarioDTO;
import com.medicamentos.service.MovimientoInventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/inventory-movements")
@RequiredArgsConstructor
public class MovimientoInventarioController {

    private final MovimientoInventarioService movimientoInventarioService;

    @GetMapping
    public List<MovimientoInventarioDTO> findAll() {
        return movimientoInventarioService.findAll();
    }

    @GetMapping("/period/{periodo}")
    public List<MovimientoInventarioDTO> findByPeriodo(@PathVariable String periodo) {
        return movimientoInventarioService.findByPeriodo(periodo);
    }

    @GetMapping("/medicine/{medicamentoId}")
    public List<MovimientoInventarioDTO> findByMedicamento(@PathVariable Long medicamentoId) {
        return movimientoInventarioService.findByMedicamento(medicamentoId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoInventarioDTO create(@Valid @RequestBody MovimientoInventarioCreateDTO request) {
        return movimientoInventarioService.create(request);
    }
}

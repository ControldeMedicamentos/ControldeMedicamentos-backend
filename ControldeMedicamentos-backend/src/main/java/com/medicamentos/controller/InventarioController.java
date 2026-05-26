package com.medicamentos.controller;

import com.medicamentos.dto.request.InventarioCreateDTO;
import com.medicamentos.dto.response.InventarioDTO;
import com.medicamentos.service.InventarioService;
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
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public List<InventarioDTO> findAll() {
        return inventarioService.findAll();
    }

    @GetMapping("/medicine/{medicamentoId}")
    public List<InventarioDTO> findByMedicamento(@PathVariable Long medicamentoId) {
        return inventarioService.findByMedicamento(medicamentoId);
    }

    @GetMapping("/low-stock")
    public List<InventarioDTO> findLowStock() {
        return inventarioService.findLowStock();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventarioDTO create(@Valid @RequestBody InventarioCreateDTO request) {
        return inventarioService.create(request);
    }
}

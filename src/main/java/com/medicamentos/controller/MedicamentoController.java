package com.medicamentos.controller;

import com.medicamentos.dto.request.MedicamentoCreateDTO;
import com.medicamentos.dto.response.MedicamentoDTO;
import com.medicamentos.service.MedicamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/medicines")
@RequiredArgsConstructor
public class MedicamentoController {

    private final MedicamentoService medicamentoService;

    @GetMapping
    public List<MedicamentoDTO> findAll() {
        return medicamentoService.findAll();
    }

    @GetMapping("/{id}")
    public MedicamentoDTO findById(@PathVariable Long id) {
        return medicamentoService.findById(id);
    }

    @GetMapping("/sismed/{codigoSismed}")
    public MedicamentoDTO findByCodigoSismed(@PathVariable String codigoSismed) {
        return medicamentoService.findByCodigoSismed(codigoSismed);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MedicamentoDTO create(@Valid @RequestBody MedicamentoCreateDTO request) {
        return medicamentoService.create(request);
    }

    @PutMapping("/{id}")
    public MedicamentoDTO update(@PathVariable Long id, @Valid @RequestBody MedicamentoCreateDTO request) {
        return medicamentoService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public MedicamentoDTO toggleActivo(@PathVariable Long id) {
        return medicamentoService.toggleActivo(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        medicamentoService.delete(id);
    }
}

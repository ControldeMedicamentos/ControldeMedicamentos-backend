package com.medicamentos.controller;

import com.medicamentos.dto.request.PacienteCreateDTO;
import com.medicamentos.dto.response.PacienteDTO;
import com.medicamentos.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;

    @GetMapping
    public List<PacienteDTO> findAll() {
        return pacienteService.findAll();
    }

    @GetMapping("/{id}")
    public PacienteDTO findById(@PathVariable Long id) {
        return pacienteService.findById(id);
    }

    @GetMapping("/document/{nroDocumento}")
    public PacienteDTO findByNroDocumento(@PathVariable String nroDocumento) {
        return pacienteService.findByNroDocumento(nroDocumento);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteDTO create(@Valid @RequestBody PacienteCreateDTO request) {
        return pacienteService.create(request);
    }

    @PutMapping("/{id}")
    public PacienteDTO update(@PathVariable Long id, @Valid @RequestBody PacienteCreateDTO request) {
        return pacienteService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public PacienteDTO toggleActivo(@PathVariable Long id) {
        return pacienteService.toggleActivo(id);
    }
}

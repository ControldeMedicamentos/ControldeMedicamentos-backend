package com.medicamentos.controller;

import com.medicamentos.dto.request.AtencionCreateDTO;
import com.medicamentos.dto.response.AtencionDTO;
import com.medicamentos.service.AtencionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AtencionController {

    private final AtencionService atencionService;

    @GetMapping
    public List<AtencionDTO> findAll() {
        return atencionService.findAll();
    }

    @GetMapping("/{id}")
    public AtencionDTO findById(@PathVariable Long id) {
        return atencionService.findById(id);
    }

    @GetMapping("/patient/{pacienteId}")
    public List<AtencionDTO> findByPaciente(@PathVariable Long pacienteId) {
        return atencionService.findByPaciente(pacienteId);
    }

    @GetMapping("/search")
    public List<AtencionDTO> findByFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return atencionService.findByFecha(desde, hasta);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AtencionDTO create(@Valid @RequestBody AtencionCreateDTO request) {
        return atencionService.create(request);
    }
}

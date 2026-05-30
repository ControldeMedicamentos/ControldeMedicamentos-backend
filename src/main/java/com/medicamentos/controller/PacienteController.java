package com.medicamentos.controller;

import com.medicamentos.domain.model.Paciente;
import com.medicamentos.domain.model.PacienteArchivo;
import com.medicamentos.dto.request.PacienteCreateDTO;
import com.medicamentos.dto.response.PacienteArchivoDTO;
import com.medicamentos.dto.response.PacienteDTO;
import com.medicamentos.exception.ResourceNotFoundException;
import com.medicamentos.repository.PacienteArchivoRepository;
import com.medicamentos.repository.PacienteRepository;
import com.medicamentos.service.AuditLogService;
import com.medicamentos.service.FileStorageService;
import com.medicamentos.service.PacienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;
    private final PacienteArchivoRepository pacienteArchivoRepository;
    private final PacienteRepository pacienteRepository;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;

    @GetMapping
    public List<PacienteDTO> findAll() {
        auditLogService.log("CONSULTAR_PACIENTES", "Pacientes", "Consulta del listado de pacientes");
        return pacienteService.findAll();
    }

    @GetMapping("/{id}")
    public PacienteDTO findById(@PathVariable Long id) {
        PacienteDTO result = pacienteService.findById(id);
        auditLogService.log("CONSULTAR_DETALLE_PACIENTE", "Pacientes",
                "Detalle del paciente: " + result.nombresApellidos() + " (ID: " + id + ")");
        return result;
    }

    @GetMapping("/document/{nroDocumento}")
    public PacienteDTO findByNroDocumento(@PathVariable String nroDocumento) {
        return pacienteService.findByNroDocumento(nroDocumento);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteDTO create(@Valid @RequestBody PacienteCreateDTO request) {
        PacienteDTO result = pacienteService.create(request);
        auditLogService.log("CREAR_PACIENTE", "Pacientes",
                "Paciente registrado: " + result.nombresApellidos() + " — Doc: " + result.nroDocumento());
        return result;
    }

    @PutMapping("/{id}")
    public PacienteDTO update(@PathVariable Long id, @Valid @RequestBody PacienteCreateDTO request) {
        PacienteDTO result = pacienteService.update(id, request);
        auditLogService.log("ACTUALIZAR_PACIENTE", "Pacientes",
                "Paciente actualizado: " + result.nombresApellidos());
        return result;
    }

    @PatchMapping("/{id}/status")
    public PacienteDTO toggleActivo(@PathVariable Long id) {
        PacienteDTO result = pacienteService.toggleActivo(id);
        String accion = result.activo() ? "ACTIVAR_PACIENTE" : "DESACTIVAR_PACIENTE";
        auditLogService.log(accion, "Pacientes",
                result.nombresApellidos() + " → " + (result.activo() ? "Activo" : "Inactivo"));
        return result;
    }

    @PostMapping("/{id}/files")
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteArchivoDTO uploadArchivo(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado: " + id));
        String storedName = fileStorageService.store(file);
        PacienteArchivo archivo = new PacienteArchivo();
        archivo.setPaciente(paciente);
        archivo.setNombreOriginal(file.getOriginalFilename() != null ? file.getOriginalFilename() : storedName);
        archivo.setNombreArchivo(storedName);
        archivo.setTipoContenido(file.getContentType());
        archivo.setTamanio(file.getSize());
        PacienteArchivo saved = pacienteArchivoRepository.save(archivo);
        auditLogService.log("SUBIR_ARCHIVO", "Pacientes",
                "Archivo subido para paciente ID " + id + ": " + file.getOriginalFilename());
        return toArchivoDTO(saved);
    }

    @GetMapping("/{id}/files")
    public List<PacienteArchivoDTO> listarArchivos(@PathVariable Long id) {
        return pacienteArchivoRepository.findByPacienteIdOrderByCreatedAtAsc(id)
                .stream().map(this::toArchivoDTO).toList();
    }

    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable Long fileId) {
        PacienteArchivo archivo = pacienteArchivoRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado: " + fileId));
        try {
            Path filePath = fileStorageService.load(archivo.getNombreArchivo());
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) throw new ResourceNotFoundException("Archivo físico no encontrado");
            String contentType = archivo.getTipoContenido() != null ? archivo.getTipoContenido() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + archivo.getNombreOriginal() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Archivo no encontrado: " + fileId);
        }
    }

    private PacienteArchivoDTO toArchivoDTO(PacienteArchivo a) {
        return new PacienteArchivoDTO(a.getId(), a.getPaciente().getId(), a.getNombreOriginal(), a.getTipoContenido(), a.getTamanio(), a.getCreatedAt());
    }
}

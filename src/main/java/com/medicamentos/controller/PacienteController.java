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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasAuthority('VISTA_PACIENTES:LEER')")
    public List<PacienteDTO> findAll() {
        return pacienteService.findAll();
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('VISTA_PACIENTES:LEER')")
    public Page<PacienteDTO> findPage(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "activos") String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by("nombresApellidos").ascending());
        return pacienteService.findPage(search, estado, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VISTA_PACIENTES:LEER')")
    public PacienteDTO findById(@PathVariable Long id) {
        PacienteDTO result = pacienteService.findById(id);
        auditLogService.log("CONSULTAR_DETALLE_PACIENTE", "Pacientes",
                "Consulta de detalle clínico", "PACIENTE", id.toString());
        return result;
    }

    @GetMapping("/document/{nroDocumento}")
    @PreAuthorize("hasAuthority('VISTA_PACIENTES:LEER')")
    public PacienteDTO findByNroDocumento(@PathVariable String nroDocumento) {
        PacienteDTO result = pacienteService.findByNroDocumento(nroDocumento);
        auditLogService.log("CONSULTAR_DETALLE_PACIENTE", "Pacientes",
                "Consulta por documento", "PACIENTE", result.id().toString());
        return result;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VISTA_PACIENTES:ESCRIBIR')")
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteDTO create(@Valid @RequestBody PacienteCreateDTO request) {
        PacienteDTO result = pacienteService.create(request);
        auditLogService.log("CREAR_PACIENTE", "Pacientes",
                "Paciente registrado", "PACIENTE", result.id().toString());
        return result;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VISTA_PACIENTES:MODIFICAR')")
    public PacienteDTO update(@PathVariable Long id, @Valid @RequestBody PacienteCreateDTO request) {
        PacienteDTO result = pacienteService.update(id, request);
        auditLogService.log("ACTUALIZAR_PACIENTE", "Pacientes",
                "Paciente actualizado", "PACIENTE", id.toString());
        return result;
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('VISTA_PACIENTES:ELIMINAR')")
    public PacienteDTO toggleActivo(@PathVariable Long id) {
        PacienteDTO result = pacienteService.toggleActivo(id);
        String accion = result.activo() ? "ACTIVAR_PACIENTE" : "DESACTIVAR_PACIENTE";
        auditLogService.log(accion, "Pacientes",
                "Estado cambiado a " + (result.activo() ? "Activo" : "Inactivo"),
                "PACIENTE", id.toString());
        return result;
    }

    @PostMapping("/{id}/files")
    @PreAuthorize("hasAuthority('VISTA_PACIENTES:ESCRIBIR')")
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
                "Archivo clínico subido: " + saved.getId(), "PACIENTE", id.toString());
        return toArchivoDTO(saved);
    }

    @GetMapping("/{id}/files")
    @PreAuthorize("hasAuthority('VISTA_PACIENTES:LEER')")
    public List<PacienteArchivoDTO> listarArchivos(@PathVariable Long id) {
        return pacienteArchivoRepository.findByPacienteIdOrderByCreatedAtAsc(id)
                .stream().map(this::toArchivoDTO).toList();
    }

    @GetMapping("/files/{fileId}/download")
    @PreAuthorize("hasAuthority('VISTA_PACIENTES:LEER')")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable Long fileId) {
        PacienteArchivo archivo = pacienteArchivoRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado: " + fileId));
        try {
            Path filePath = fileStorageService.load(archivo.getNombreArchivo());
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) throw new ResourceNotFoundException("Archivo físico no encontrado");
            auditLogService.log("DESCARGAR_ARCHIVO", "Pacientes", "Archivo clínico consultado: " + fileId,
                    "PACIENTE", archivo.getPaciente().getId().toString());
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

package com.medicamentos.controller;

import com.medicamentos.domain.model.AtencionArchivo;
import com.medicamentos.dto.request.AtencionCreateDTO;
import com.medicamentos.dto.response.AtencionArchivoDTO;
import com.medicamentos.dto.response.AtencionDTO;
import com.medicamentos.exception.ResourceNotFoundException;
import com.medicamentos.repository.AtencionArchivoRepository;
import com.medicamentos.repository.AtencionRepository;
import com.medicamentos.service.AtencionService;
import com.medicamentos.service.AuditLogService;
import com.medicamentos.service.FileStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AtencionController {

    private final AtencionService atencionService;
    private final AtencionArchivoRepository archivoRepository;
    private final AtencionRepository atencionRepository;
    private final FileStorageService fileStorageService;
    private final AuditLogService auditLogService;

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
        auditLogService.log("CONSULTAR_ATENCIONES_PACIENTE", "Atenciones",
                "Consulta de atenciones del paciente ID " + pacienteId);
        return atencionService.findByPaciente(pacienteId);
    }

    @GetMapping("/search")
    public List<AtencionDTO> findByFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        auditLogService.log("CONSULTAR_ATENCIONES", "Atenciones",
                "Consulta de atenciones del " + desde + " al " + hasta);
        return atencionService.findByFecha(desde, hasta);
    }

    @GetMapping("/page")
    public Page<AtencionDTO> findPageByFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Order.desc("fechaEvaluacion"), Sort.Order.desc("id")));
        auditLogService.log("CONSULTAR_ATENCIONES", "Atenciones",
                "Consulta paginada de atenciones del " + desde + " al " + hasta);
        return atencionService.findPageByFecha(desde, hasta, search, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AtencionDTO create(@Valid @RequestBody AtencionCreateDTO request, Authentication authentication) {
        AtencionDTO result = atencionService.create(withAuthenticatedUser(request, authentication.getName()));
        auditLogService.log("CREAR_ATENCION", "Atenciones",
                "Atención registrada para paciente ID " + request.pacienteId()
                + " — Motivo: " + request.motivo()
                + (result.consumos() != null && !result.consumos().isEmpty()
                        ? " — " + result.consumos().size() + " medicamento(s) consumido(s)" : ""));
        return result;
    }

    @PostMapping("/{id}/files")
    @ResponseStatus(HttpStatus.CREATED)
    public AtencionArchivoDTO uploadArchivo(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        var atencion = atencionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Atención no encontrada: " + id));
        String storedName = fileStorageService.store(file);
        AtencionArchivo archivo = new AtencionArchivo();
        archivo.setAtencion(atencion);
        archivo.setNombreOriginal(file.getOriginalFilename() != null ? file.getOriginalFilename() : storedName);
        archivo.setNombreArchivo(storedName);
        archivo.setTipoContenido(file.getContentType());
        archivo.setTamanio(file.getSize());
        AtencionArchivo saved = archivoRepository.save(archivo);
        return toArchivoDTO(saved);
    }

    @GetMapping("/{id}/files")
    public List<AtencionArchivoDTO> listarArchivos(@PathVariable Long id) {
        return archivoRepository.findByAtencionIdOrderByCreatedAtAsc(id)
                .stream().map(this::toArchivoDTO).toList();
    }

    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable Long fileId) {
        AtencionArchivo archivo = archivoRepository.findById(fileId)
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

    private AtencionArchivoDTO toArchivoDTO(AtencionArchivo a) {
        return new AtencionArchivoDTO(a.getId(), a.getAtencion().getId(), a.getNombreOriginal(), a.getTipoContenido(), a.getTamanio(), a.getCreatedAt());
    }

    private AtencionCreateDTO withAuthenticatedUser(AtencionCreateDTO request, String username) {
        return new AtencionCreateDTO(
                request.pacienteId(), request.fechaEvaluacion(), request.motivo(),
                request.antecedentes(), request.inmunizaciones(), request.signosVitales(),
                request.examenFisico(), request.laboratorio(),
                request.diagnostico1(), request.cie101(), request.tipoDiagnostico1(),
                request.diagnostico2(), request.cie102(), request.tipoDiagnostico2(),
                request.diagnostico3(), request.cie103(), request.tipoDiagnostico3(),
                request.conclusion(), request.derivacion(), request.observaciones(),
                username, request.consumos()
        );
    }
}

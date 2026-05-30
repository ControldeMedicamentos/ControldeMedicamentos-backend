package com.medicamentos.service.Impl;

import com.medicamentos.domain.model.Usuario;
import com.medicamentos.dto.request.EmpleadoCreateDTO;
import com.medicamentos.dto.request.EmpleadoUpdateDTO;
import com.medicamentos.dto.response.EmpleadoDTO;
import com.medicamentos.exception.DuplicateResourceException;
import com.medicamentos.exception.ResourceNotFoundException;
import com.medicamentos.repository.UsuarioRepository;
import com.medicamentos.service.EmailService;
import com.medicamentos.service.EmpleadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpleadoServiceImpl implements EmpleadoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    @Transactional(readOnly = true)
    public List<EmpleadoDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    public EmpleadoDTO create(EmpleadoCreateDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException("El email '" + dto.email() + "' ya está registrado");
        }
        String username = generarUsername(dto.email());
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(dto.dni()));
        u.setNombre(dto.nombre().trim());
        u.setEmail(dto.email().trim().toLowerCase());
        u.setRol(dto.rol());
        u.setActivo(true);
        u.setMustChangePassword(true);
        EmpleadoDTO resultado = toDTO(usuarioRepository.save(u));
        emailService.sendBienvenida(dto.nombre().trim(), dto.email().trim().toLowerCase(), dto.dni());
        return resultado;
    }

    private String generarUsername(String email) {
        String base = email.split("@")[0].toLowerCase().replaceAll("[^a-z0-9]", "");
        if (base.isEmpty()) base = "usuario";
        String username = base;
        int i = 2;
        while (usuarioRepository.existsByUsername(username)) {
            username = base + i++;
        }
        return username;
    }

    @Override
    @Transactional
    public EmpleadoDTO update(Long id, EmpleadoUpdateDTO dto) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + id));

        if (!u.getEmail().equalsIgnoreCase(dto.email()) && usuarioRepository.existsByEmail(dto.email())) {
            throw new DuplicateResourceException("El email '" + dto.email() + "' ya está registrado");
        }

        u.setNombre(dto.nombre().trim());
        u.setEmail(dto.email().trim().toLowerCase());
        u.setRol(dto.rol());
        return toDTO(usuarioRepository.save(u));
    }

    @Override
    @Transactional
    public EmpleadoDTO toggleEstado(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado no encontrado: " + id));
        u.setActivo(!u.getActivo());
        return toDTO(usuarioRepository.save(u));
    }

    private EmpleadoDTO toDTO(Usuario u) {
        return new EmpleadoDTO(
                u.getId(),
                u.getUsername(),
                u.getNombre(),
                u.getEmail(),
                u.getRol().name(),
                u.getActivo(),
                u.getCreatedAt()
        );
    }
}

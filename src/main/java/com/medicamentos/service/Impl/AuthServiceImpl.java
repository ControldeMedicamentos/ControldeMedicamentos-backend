package com.medicamentos.service.Impl;

import com.medicamentos.domain.model.Usuario;
import com.medicamentos.dto.request.ChangePasswordDTO;
import com.medicamentos.dto.request.LoginRequestDTO;
import com.medicamentos.dto.request.RegisterRequestDTO;
import com.medicamentos.dto.response.AuthResponseDTO;
import com.medicamentos.exception.DuplicateResourceException;
import com.medicamentos.exception.ResourceNotFoundException;
import com.medicamentos.repository.UsuarioRepository;
import com.medicamentos.security.JwtUtil;
import com.medicamentos.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmail(request.username())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                usuario.getUsername(),
                request.password()
        ));
        return buildResponse(usuario);
    }

    @Override
    public AuthResponseDTO changePassword(String username, ChangePasswordDTO dto) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (!passwordEncoder.matches(dto.currentPassword(), usuario.getPassword())) {
            throw new BadCredentialsException("La contraseña actual es incorrecta");
        }
        if (dto.newPassword().equals(dto.currentPassword())) {
            throw new IllegalArgumentException("La nueva contraseña debe ser diferente a la actual");
        }
        usuario.setPassword(passwordEncoder.encode(dto.newPassword()));
        usuario.setMustChangePassword(false);
        return buildResponse(usuarioRepository.save(usuario));
    }

    @Override
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Ya existe un usuario con username: " + request.username());
        }
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Ya existe un usuario con email: " + request.email());
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(request.username());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setNombre(request.nombre());
        usuario.setEmail(request.email());
        usuario.setRol(request.rol());
        usuario.setActivo(true);
        return buildResponse(usuarioRepository.save(usuario));
    }

    private AuthResponseDTO buildResponse(Usuario usuario) {
        return new AuthResponseDTO(
                jwtUtil.generateToken(usuario),
                jwtUtil.getExpiration(),
                usuario.getUsername(),
                usuario.getRol(),
                Boolean.TRUE.equals(usuario.getMustChangePassword())
        );
    }
}

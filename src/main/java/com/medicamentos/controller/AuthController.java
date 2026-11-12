package com.medicamentos.controller;

import com.medicamentos.dto.request.ChangePasswordDTO;
import com.medicamentos.dto.request.LoginRequestDTO;
import com.medicamentos.dto.request.RegisterRequestDTO;
import com.medicamentos.dto.request.ResetPasswordDTO;
import com.medicamentos.dto.response.AuthResponseDTO;
import com.medicamentos.service.AuditLogService;
import com.medicamentos.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuditLogService auditLogService;

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        try {
            AuthResponseDTO response = authService.login(request);
            auditLogService.logLogin(request.username(), response.rol().name(), true);
            return response;
        } catch (AuthenticationException exception) {
            auditLogService.logLogin(request.username(), "DESCONOCIDO", false);
            throw exception;
        }
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResponseDTO response = authService.register(request);
        auditLogService.log("REGISTRAR_USUARIO", "Seguridad", "Usuario registrado",
                "USUARIO", response.username());
        return response;
    }

    @PatchMapping("/change-password")
    public AuthResponseDTO changePassword(@Valid @RequestBody ChangePasswordDTO dto, Authentication auth) {
        AuthResponseDTO response = authService.changePassword(auth.getName(), dto);
        auditLogService.log("CAMBIO_CONTRASENA", "Seguridad", "Cambio de contraseña exitoso",
                "USUARIO", auth.getName());
        return response;
    }

    @GetMapping("/me")
    public ResponseEntity<java.util.Map<String, Object>> me(Authentication auth) {
        java.util.List<String> permisos = authService.getPermisos(auth.getName());
        return ResponseEntity.ok(java.util.Map.of("permisos", permisos));
    }

    @GetMapping("/reset-info")
    public ResponseEntity<java.util.Map<String, String>> resetInfo(@RequestParam String token) {
        String nombre = authService.getNombreByResetToken(token);
        return ResponseEntity.ok(java.util.Map.of("nombre", nombre));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO dto) {
        authService.resetPassword(dto.token(), dto.newPassword());
        auditLogService.logRequestEvent("RESTABLECER_CONTRASENA", "Seguridad",
                "Contraseña restablecida mediante token válido", "EXITOSO");
        return ResponseEntity.ok().build();
    }
}

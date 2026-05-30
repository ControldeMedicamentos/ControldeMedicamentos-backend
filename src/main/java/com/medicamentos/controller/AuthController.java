package com.medicamentos.controller;

import com.medicamentos.dto.request.ChangePasswordDTO;
import com.medicamentos.dto.request.LoginRequestDTO;
import com.medicamentos.dto.request.RegisterRequestDTO;
import com.medicamentos.dto.response.AuthResponseDTO;
import com.medicamentos.service.AuditLogService;
import com.medicamentos.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuditLogService auditLogService;

    @PostMapping("/login")
    public AuthResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        auditLogService.logLogin(request.username(), response.rol().name(), true);
        return response;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO request) {
        return authService.register(request);
    }

    @PatchMapping("/change-password")
    public AuthResponseDTO changePassword(@Valid @RequestBody ChangePasswordDTO dto, Authentication auth) {
        AuthResponseDTO response = authService.changePassword(auth.getName(), dto);
        auditLogService.log("CAMBIO_CONTRASENA", "Seguridad", "Cambio de contraseña exitoso");
        return response;
    }
}

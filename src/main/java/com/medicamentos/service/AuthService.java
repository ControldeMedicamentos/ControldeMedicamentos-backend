package com.medicamentos.service;

import com.medicamentos.dto.request.ChangePasswordDTO;
import com.medicamentos.dto.request.LoginRequestDTO;
import com.medicamentos.dto.request.RegisterRequestDTO;
import com.medicamentos.dto.response.AuthResponseDTO;

public interface AuthService {

    AuthResponseDTO login(LoginRequestDTO request);

    AuthResponseDTO register(RegisterRequestDTO request);

    AuthResponseDTO changePassword(String username, ChangePasswordDTO dto);

    void resetPassword(String token, String newPassword);

    String getNombreByResetToken(String token);

    java.util.List<String> getPermisos(String username);
}

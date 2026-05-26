package com.medicamentos.service;

import com.medicamentos.dto.request.LoginRequestDTO;
import com.medicamentos.dto.request.RegisterRequestDTO;
import com.medicamentos.dto.response.AuthResponseDTO;

public interface AuthService {

    AuthResponseDTO login(LoginRequestDTO request);

    AuthResponseDTO register(RegisterRequestDTO request);
}

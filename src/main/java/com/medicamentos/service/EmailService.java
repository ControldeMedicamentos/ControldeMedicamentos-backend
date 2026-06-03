package com.medicamentos.service;

public interface EmailService {
    void sendResetPassword(String nombre, String email, String resetLink);
}

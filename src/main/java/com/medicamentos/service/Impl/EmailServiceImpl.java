package com.medicamentos.service.Impl;

import com.medicamentos.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@sismed.pe}")
    private String from;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    private boolean isMailConfigured() {
        return mailSender != null && mailUsername != null && !mailUsername.isBlank();
    }

    @Override
    public void sendResetPassword(String nombre, String email, String resetLink) {
        if (!isMailConfigured()) {
            log.warn("Email SMTP no configurado. Enlace de activación para {}: {}", email, resetLink);
            return;
        }
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("Activa tu cuenta — SISMED Control de Medicamentos");
            helper.setText(buildResetHtml(nombre, resetLink), true);
            mailSender.send(message);
            log.info("Email de activación enviado a: {}", email);
        } catch (Exception e) {
            log.error("Error enviando correo a {}: {} | Causa: {}", email, e.getMessage(),
                    e.getCause() != null ? e.getCause().getMessage() : "sin causa");
        }
    }

    private String buildResetHtml(String nombre, String resetLink) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <body style="margin:0;padding:0;background:#f1f5f9;font-family:Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0">
                <tr><td align="center" style="padding:32px 16px;">
                  <table width="560" cellpadding="0" cellspacing="0" style="background:#fff;border-radius:10px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08);">
                    <tr>
                      <td style="background:#1d5f9f;padding:24px 32px;">
                        <p style="margin:0;color:#fff;font-size:18px;font-weight:bold;">SISMED — Control de Medicamentos</p>
                        <p style="margin:4px 0 0;color:#bfdbfe;font-size:13px;">Tópico de Salud · La Libertad, Perú</p>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:32px;">
                        <p style="margin:0 0 8px;color:#1f2937;font-size:15px;">Hola, <strong>%s</strong></p>
                        <p style="margin:0 0 24px;color:#4b5563;font-size:14px;line-height:1.7;">
                          El administrador ha creado tu cuenta en el sistema SISMED.<br>
                          Haz clic en el botón de abajo para establecer tu contraseña y activar tu acceso.
                        </p>
                        <table cellpadding="0" cellspacing="0" style="margin-bottom:24px;">
                          <tr>
                            <td style="background:#1d5f9f;border-radius:7px;">
                              <a href="%s" style="display:inline-block;padding:13px 32px;color:#fff;text-decoration:none;font-weight:bold;font-size:14px;">
                                Establecer contraseña →
                              </a>
                            </td>
                          </tr>
                        </table>
                        <table width="100%%" cellpadding="0" cellspacing="0" style="background:#fef9c3;border:1px solid #fde68a;border-radius:8px;margin-bottom:20px;">
                          <tr><td style="padding:12px 18px;">
                            <p style="margin:0;color:#92400e;font-size:13px;">
                              ⏱ Este enlace expira en <strong>48 horas</strong>. Si no lo usas, contacta al administrador.
                            </p>
                          </td></tr>
                        </table>
                        <p style="margin:0;color:#94a3b8;font-size:12px;line-height:1.6;">
                          Si no esperabas este mensaje, ignóralo o contacta al administrador del sistema.
                        </p>
                      </td>
                    </tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(nombre, resetLink);
    }
}

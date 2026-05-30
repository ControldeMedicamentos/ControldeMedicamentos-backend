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

    @Override
    public void sendBienvenida(String nombre, String email, String dni) {
        if (mailSender == null) {
            log.warn("JavaMailSender no configurado. Email de bienvenida no enviado a: {}", email);
            return;
        }
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(email);
            helper.setSubject("Bienvenido al Sistema SISMED - Control de Medicamentos");
            helper.setText(buildHtml(nombre, email, dni), true);
            mailSender.send(message);
            log.info("Email de bienvenida enviado a: {}", email);
        } catch (Exception e) {
            log.error("Error enviando correo a {}: {}", email, e.getMessage());
        }
    }

    private String buildHtml(String nombre, String email, String dni) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <body style="margin:0;padding:0;background:#f1f5f9;font-family:Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0">
                <tr><td align="center" style="padding:32px 16px;">
                  <table width="580" cellpadding="0" cellspacing="0" style="background:#fff;border-radius:10px;overflow:hidden;box-shadow:0 2px 12px rgba(0,0,0,0.08);">
                    <tr>
                      <td style="background:#1d5f9f;padding:24px 32px;">
                        <p style="margin:0;color:#fff;font-size:18px;font-weight:bold;">SISMED — Control de Medicamentos</p>
                        <p style="margin:4px 0 0;color:#bfdbfe;font-size:13px;">Tópico de Salud · La Libertad, Perú</p>
                      </td>
                    </tr>
                    <tr>
                      <td style="padding:32px;">
                        <p style="margin:0 0 12px;color:#1f2937;font-size:15px;">Hola, <strong>%s</strong></p>
                        <p style="margin:0 0 20px;color:#4b5563;font-size:14px;line-height:1.6;">
                          El administrador ha creado tu cuenta en el sistema de gestión de medicamentos SISMED.
                        </p>
                        <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;margin-bottom:20px;">
                          <tr><td style="padding:16px 20px;">
                            <p style="margin:0 0 10px;font-size:11px;font-weight:bold;color:#94a3b8;text-transform:uppercase;letter-spacing:0.06em;">Tus credenciales de acceso</p>
                            <p style="margin:4px 0;color:#1f2937;font-size:14px;"><strong>Email:</strong> %s</p>
                            <p style="margin:4px 0;color:#1f2937;font-size:14px;"><strong>Contraseña temporal:</strong> %s (tu DNI)</p>
                          </td></tr>
                        </table>
                        <table width="100%%" cellpadding="0" cellspacing="0" style="background:#fef9c3;border:1px solid #fde68a;border-radius:8px;margin-bottom:24px;">
                          <tr><td style="padding:14px 20px;">
                            <p style="margin:0;color:#92400e;font-size:13px;">
                              ⚠️ Por seguridad, deberás <strong>cambiar tu contraseña</strong> al ingresar por primera vez.
                            </p>
                          </td></tr>
                        </table>
                        <a href="%s/login" style="display:inline-block;background:#1d5f9f;color:#fff;padding:12px 28px;border-radius:7px;text-decoration:none;font-weight:bold;font-size:14px;">
                          Iniciar sesión →
                        </a>
                        <p style="margin:24px 0 0;color:#94a3b8;font-size:12px;">
                          Si no reconoces este registro, contacta al administrador del sistema.
                        </p>
                      </td>
                    </tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(nombre, email, dni, frontendUrl);
    }
}

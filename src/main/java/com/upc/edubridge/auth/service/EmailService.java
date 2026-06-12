package com.upc.edubridge.auth.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EmailService {
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.resend.com")
            .defaultHeader("Authorization", "Bearer re_FQ4Q8ckX_Ai2m4WGjg2JwDKDhB5uTTYG7")
            .build();

    public void sendRecoveryEmail(String to, String code) {
        String body = String.format(
                "{\"from\": \"EduBridge <noreply@edubrigde.me>\", \"to\": \"%s\", \"subject\": \"Código de Recuperación de EduBridge\", \"html\": \"<div style='font-family: sans-serif; padding: 20px; max-width: 500px; border: 1px solid #e2e8f0; rounded: 12px;'><h2 style='color: #1e3a8a;'>Recuperación de Contraseña</h2><p>Hemos recibido una solicitud para cambiar tu contraseña de EduBridge.</p><p>Usa el siguiente código de verificación de 6 dígitos:</p><div style='background: #f1f5f9; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 4px; color: #ea580c; border-radius: 8px;'>%s</div><p style='font-size: 12px; color: #64748b; margin-top: 20px;'>Este código expirará en 15 minutos. Si no realizaste esta solicitud, puedes ignorar este correo de forma segura.</p></div>\"}",
                to, code
        );
        webClient.post().uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }
}
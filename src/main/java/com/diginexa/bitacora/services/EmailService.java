package com.diginexa.bitacora.services;

import com.diginexa.bitacora.constants.PrioridadNotificacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * Servicio para envío de correos electrónicos.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@bitacora-digital.com}")
    private String fromEmail;

    @Value("${app.name:Bitácora Digital}")
    private String appName;

    /**
     * Envía una notificación por correo electrónico.
     */
    public void enviarNotificacion(
            String destinatario,
            String nombreDestinatario,
            String titulo,
            String mensaje,
            PrioridadNotificacion prioridad) {

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject(String.format("[%s] %s", appName, titulo));

            String contenidoHtml = construirPlantillaHtml(nombreDestinatario, titulo, mensaje, prioridad);
            helper.setText(contenidoHtml, true);

            mailSender.send(mimeMessage);
            log.info("Email enviado exitosamente a {}", destinatario);

        } catch (Exception e) {
            log.error("Error enviando email a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Error enviando email", e);
        }
    }

    private String construirPlantillaHtml(
            String nombreDestinatario,
            String titulo,
            String mensaje,
            PrioridadNotificacion prioridad) {

        String colorPrioridad = obtenerColorPrioridad(prioridad);
        String etiquetaPrioridad = prioridad.getDescripcion();

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: %s; color: white; padding: 20px; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f9f9f9; padding: 20px; border: 1px solid #ddd; border-top: none; }
                    .footer { background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #666; border-radius: 0 0 8px 8px; }
                    .priority-badge { display: inline-block; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2 style="margin: 0;">%s</h2>
                        <span class="priority-badge" style="background-color: rgba(255,255,255,0.2); margin-top: 10px;">%s</span>
                    </div>
                    <div class="content">
                        <p>Hola <strong>%s</strong>,</p>
                        <p>%s</p>
                        <p style="margin-top: 20px;">
                            <a href="#" style="background-color: %s; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">
                                Ver en Bitácora Digital
                            </a>
                        </p>
                    </div>
                    <div class="footer">
                        <p>Este es un mensaje automático de %s.</p>
                        <p>Por favor, no responda a este correo.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
            colorPrioridad,
            titulo,
            etiquetaPrioridad,
            nombreDestinatario,
            mensaje,
            colorPrioridad,
            appName
        );
    }

    private String obtenerColorPrioridad(PrioridadNotificacion prioridad) {
        return switch (prioridad) {
            case CRITICO -> "#dc3545";
            case ALERTA -> "#ffc107";
            case INFO -> "#17a2b8";
            case SUCCESS -> "#28a745";
        };
    }
}

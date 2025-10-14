package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

@Schema(description = "Datos para envío de correo electrónico")
public record EmailDTO(
        @Schema(description = "Asunto del correo", example = "Código de verificación", required = true, maxLength = 100)
        @NotBlank(message = "Subject cannot be empty")
        @Length(max = 100, message = "Subject must not exceed 100 characters")
        String subject,

        @Schema(description = "Contenido del correo", example = "Su código de verificación es: 123456", required = true)
        @NotBlank(message = "Body cannot be empty")
        String body,

        @Schema(description = "Correo electrónico del destinatario", example = "usuario@email.com", required = true)
        @NotBlank(message = "Receiver email cannot be empty")
        @Email(message = "Invalid email format for receiver")
        String receiver
) {
}

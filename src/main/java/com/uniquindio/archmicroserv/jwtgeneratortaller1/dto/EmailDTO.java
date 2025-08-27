package com.uniquindio.archmicroserv.jwtgeneratortaller1.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record EmailDTO(
        @NotBlank(message = "Subject cannot be empty")
        @Length(max = 100, message = "Subject must not exceed 100 characters")
        String subject,

        @NotBlank(message = "Body cannot be empty")
        String body,

        @NotBlank(message = "Receiver email cannot be empty")
        @Email(message = "Invalid email format for receiver")
        String receiver
) {
}

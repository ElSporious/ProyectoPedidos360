package com.example.adminrabbit.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record QueueRequest(
        @NotBlank(message = "El nombre de la cola es obligatorio")
        @Size(max = 255, message = "El nombre de la cola no puede superar los 255 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "El nombre solo admite letras, numeros, punto, guion y guion bajo")
        String name,
        Boolean durable,
        String deadLetterExchange,
        String deadLetterRoutingKey,
        @Min(value = 1, message = "ttlMs debe ser mayor a 0")
        Integer ttlMs,
        @Min(value = 1, message = "maxLength debe ser mayor a 0")
        Integer maxLength
) {

}

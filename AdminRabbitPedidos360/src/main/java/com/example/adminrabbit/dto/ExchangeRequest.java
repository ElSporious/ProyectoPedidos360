package com.example.adminrabbit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ExchangeRequest(
        @NotBlank(message = "El nombre del exchange es obligatorio")
        @Size(max = 255, message = "El nombre del exchange no puede superar los 255 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "El nombre solo admite letras, numeros, punto, guion y guion bajo")
        String name,
        @NotBlank(message = "El tipo de exchange es obligatorio")
        @Pattern(regexp = "^(direct|topic|fanout)$", message = "El tipo debe ser direct, topic o fanout")
        String type,
        Boolean durable
) {

}

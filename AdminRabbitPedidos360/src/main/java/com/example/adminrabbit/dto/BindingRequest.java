package com.example.adminrabbit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BindingRequest(
        @NotBlank(message = "La cola es obligatoria")
        String queue,
        @NotBlank(message = "El exchange es obligatorio")
        String exchange,
        @NotNull(message = "La routing key es obligatoria (puede ir vacia en fanout)")
        String routingKey
) {

}

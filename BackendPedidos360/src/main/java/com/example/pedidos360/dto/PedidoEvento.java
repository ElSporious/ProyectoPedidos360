package com.example.pedidos360.dto;

import java.time.LocalDateTime;

public record PedidoEvento(
        Long pedidoId,
        String estado,
        String clienteEmail,
        LocalDateTime timestamp
) {}
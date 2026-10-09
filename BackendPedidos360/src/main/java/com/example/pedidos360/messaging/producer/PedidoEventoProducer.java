package com.example.pedidos360.messaging.producer;

import com.example.pedidos360.dto.PedidoEvento;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PedidoEventoProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public PedidoEventoProducer(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void publicar(String routingKey, PedidoEvento evento) {
        try {
            String json = objectMapper.writeValueAsString(evento);
            rabbitTemplate.convertAndSend("pedidos360.direct.exchange", routingKey, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar el evento de pedido", e);
        }
    }
}
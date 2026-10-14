package com.example.pedidos360.messaging.producer;

import com.example.pedidos360.config.RabbitMQConfig;
import com.example.pedidos360.dto.PedidoEvento;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

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
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, json);
            rabbitTemplate.convertAndSend(RabbitMQConfig.TOPIC_EXCHANGE, routingKey, json);
        } catch (JacksonException e) {
            throw new RuntimeException("Error al serializar el evento de pedido", e);
        }
    }
}
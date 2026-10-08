package com.example.pedidos360.messaging.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PedidoEventoProducer {
    private final RabbitTemplate rabbitTemplate;

    public PedidoEventoProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publicar(String routingKey, String mensajeJson) {
        rabbitTemplate.convertAndSend("pedidos360.direct.exchange", routingKey, mensajeJson);
    }
}

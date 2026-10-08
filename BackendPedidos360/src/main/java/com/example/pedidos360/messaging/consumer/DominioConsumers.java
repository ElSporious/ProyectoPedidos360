package com.example.pedidos360.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class DominioConsumers {
    @RabbitListener(queues = "notificaciones.queue")
    public void notificar(String eventoJson) {
        System.out.println("[NOTIFICACIÓN] " + eventoJson);
    }
}

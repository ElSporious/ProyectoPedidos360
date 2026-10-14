package com.example.pedidos360.messaging.consumer;

import com.example.pedidos360.config.RabbitMQConfig;
import com.example.pedidos360.dto.PedidoEvento;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Slf4j
@Service
public class DominioConsumers {

    private final ObjectMapper objectMapper;

    public DominioConsumers(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICACIONES, ackMode = "MANUAL")
    public void notificar(String eventoJson, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        procesar("NOTIFICACIÓN", eventoJson, channel, tag);
    }

    @RabbitListener(queues = RabbitMQConfig.DESPACHO, ackMode = "MANUAL")
    public void despachar(String eventoJson, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        procesar("DESPACHO", eventoJson, channel, tag);
    }

    @RabbitListener(queues = RabbitMQConfig.AUDITORIA, ackMode = "MANUAL")
    public void auditar(String eventoJson, Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        procesar("AUDITORÍA", eventoJson, channel, tag);
    }

    private void procesar(String dominio, String eventoJson, Channel channel, long tag) throws IOException {
        try {
            PedidoEvento evento = objectMapper.readValue(eventoJson, PedidoEvento.class);
            if (evento.pedidoId() == null || evento.estado() == null) {
                throw new IllegalArgumentException("Evento de pedido incompleto");
            }
            log.info("[{}] {}", dominio, eventoJson);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("[{}] Mensaje rechazado y enviado a la DLQ: {} | motivo: {}", dominio, eventoJson, e.getMessage());
            channel.basicNack(tag, false, false);
        }
    }
}

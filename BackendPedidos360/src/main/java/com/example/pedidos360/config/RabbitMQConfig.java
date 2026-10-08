package com.example.pedidos360.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "pedidos360.direct.exchange";
    public static final String NOTIFICACIONES = "notificaciones.queue";

    @Bean
    DirectExchange pedidosExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue notificacionesQueue() {
        return new Queue(NOTIFICACIONES, true);
    }

    @Bean
    Binding notificaAceptado(DirectExchange ex, Queue notificacionesQueue) {
        return BindingBuilder.bind(notificacionesQueue).to(ex).with("pedido.aceptado");
    }
}

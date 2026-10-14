package com.example.pedidos360.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "pedidos360.direct.exchange";
    public static final String TOPIC_EXCHANGE = "pedidos360.topic.exchange";
    public static final String DLX_EXCHANGE = "pedidos360.dlx.exchange";

    public static final String NOTIFICACIONES = "notificaciones.queue";
    public static final String DESPACHO = "despacho.queue";
    public static final String AUDITORIA = "auditoria.queue";

    public static final String NOTIFICACIONES_DLQ = "notificaciones.dlq";
    public static final String DESPACHO_DLQ = "despacho.dlq";
    public static final String AUDITORIA_DLQ = "auditoria.dlq";

    public static final String RK_CREADO = "pedido.creado";
    public static final String RK_ACEPTADO = "pedido.aceptado";
    public static final String RK_DESPACHADO = "pedido.despachado";
    public static final String RK_ACTUALIZADO = "pedido.actualizado";
    public static final String RK_TODOS = "pedido.#";

    @Bean
    DirectExchange pedidosExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    TopicExchange pedidosTopicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE, true, false);
    }

    @Bean
    DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }

    @Bean
    Queue notificacionesQueue() {
        return QueueBuilder.durable(NOTIFICACIONES)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(NOTIFICACIONES_DLQ)
                .build();
    }

    @Bean
    Queue despachoQueue() {
        return QueueBuilder.durable(DESPACHO)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(DESPACHO_DLQ)
                .build();
    }

    @Bean
    Queue auditoriaQueue() {
        return QueueBuilder.durable(AUDITORIA)
                .deadLetterExchange(DLX_EXCHANGE)
                .deadLetterRoutingKey(AUDITORIA_DLQ)
                .build();
    }

    @Bean
    Queue notificacionesDlq() {
        return new Queue(NOTIFICACIONES_DLQ, true);
    }

    @Bean
    Queue despachoDlq() {
        return new Queue(DESPACHO_DLQ, true);
    }

    @Bean
    Queue auditoriaDlq() {
        return new Queue(AUDITORIA_DLQ, true);
    }

    @Bean
    Binding notificaAceptado() {
        return BindingBuilder.bind(notificacionesQueue()).to(pedidosExchange()).with(RK_ACEPTADO);
    }

    @Bean
    Binding despachaDespachado() {
        return BindingBuilder.bind(despachoQueue()).to(pedidosExchange()).with(RK_DESPACHADO);
    }

    @Bean
    Binding auditaTodos() {
        return BindingBuilder.bind(auditoriaQueue()).to(pedidosTopicExchange()).with(RK_TODOS);
    }

    @Bean
    Binding notificacionesDlqBinding() {
        return BindingBuilder.bind(notificacionesDlq()).to(dlxExchange()).with(NOTIFICACIONES_DLQ);
    }

    @Bean
    Binding despachoDlqBinding() {
        return BindingBuilder.bind(despachoDlq()).to(dlxExchange()).with(DESPACHO_DLQ);
    }

    @Bean
    Binding auditoriaDlqBinding() {
        return BindingBuilder.bind(auditoriaDlq()).to(dlxExchange()).with(AUDITORIA_DLQ);
    }
}

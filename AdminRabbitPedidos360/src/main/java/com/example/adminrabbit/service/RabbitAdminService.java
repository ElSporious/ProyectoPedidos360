package com.example.adminrabbit.service;

import com.example.adminrabbit.dto.BindingRequest;
import com.example.adminrabbit.dto.ExchangeRequest;
import com.example.adminrabbit.dto.QueueRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RabbitAdminService {

    private final AmqpAdmin amqpAdmin;

    public void createQueue(QueueRequest request) {
        validarNombre(request.name());

        boolean tieneDlx = tieneTexto(request.deadLetterExchange());
        boolean tieneDlxKey = tieneTexto(request.deadLetterRoutingKey());
        if (tieneDlxKey && !tieneDlx) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "deadLetterRoutingKey requiere deadLetterExchange");
        }

        QueueBuilder builder = Boolean.FALSE.equals(request.durable())
                ? QueueBuilder.nonDurable(request.name())
                : QueueBuilder.durable(request.name());
        if (tieneDlx) {
            builder.deadLetterExchange(request.deadLetterExchange());
        }
        if (tieneDlxKey) {
            builder.deadLetterRoutingKey(request.deadLetterRoutingKey());
        }
        if (request.ttlMs() != null) {
            builder.ttl(request.ttlMs());
        }
        if (request.maxLength() != null) {
            builder.maxLength(request.maxLength());
        }

        ejecutar(() -> amqpAdmin.declareQueue(builder.build()));
    }

    public Map<String, Object> getQueue(String name) {
        validarNombre(name);
        QueueInformation info = amqpAdmin.getQueueInfo(name);
        if (info == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La cola no existe: " + name);
        }
        return Map.of(
                "name", info.getName(),
                "messages", info.getMessageCount(),
                "consumers", info.getConsumerCount()
        );
    }

    public void deleteQueue(String name) {
        validarNombre(name);
        if (amqpAdmin.getQueueInfo(name) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La cola no existe: " + name);
        }
        ejecutar(() -> amqpAdmin.deleteQueue(name));
    }

    public void createExchange(ExchangeRequest request) {
        validarNombre(request.name());

        boolean durable = !Boolean.FALSE.equals(request.durable());
        Exchange exchange = switch (request.type().toLowerCase()) {
            case "direct" -> new DirectExchange(request.name(), durable, false);
            case "topic" -> new TopicExchange(request.name(), durable, false);
            case "fanout" -> new FanoutExchange(request.name(), durable, false);
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tipo de exchange invalido: " + request.type());
        };

        ejecutar(() -> amqpAdmin.declareExchange(exchange));
    }

    public void deleteExchange(String name) {
        validarNombre(name);
        ejecutar(() -> amqpAdmin.deleteExchange(name));
    }

    public void createBinding(BindingRequest request) {
        ejecutar(() -> amqpAdmin.declareBinding(construirBinding(request)));
    }

    public void deleteBinding(BindingRequest request) {
        ejecutar(() -> amqpAdmin.removeBinding(construirBinding(request)));
    }

    private Binding construirBinding(BindingRequest request) {
        validarNombre(request.queue());
        validarNombre(request.exchange());
        if (amqpAdmin.getQueueInfo(request.queue()) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La cola no existe: " + request.queue());
        }
        return new Binding(request.queue(), Binding.DestinationType.QUEUE,
                request.exchange(), request.routingKey(), null);
    }

    private void validarNombre(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre no puede estar vacio");
        }
        if (!name.matches("^[A-Za-z0-9._-]{1,255}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre invalido: " + name);
        }
        if (name.startsWith("amq.")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El prefijo amq. esta reservado por RabbitMQ");
        }
    }

    private boolean tieneTexto(String value) {
        return value != null && !value.isBlank();
    }

    private void ejecutar(Runnable operacion) {
        try {
            operacion.run();
        } catch (AmqpException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "RabbitMQ rechazo la operacion: " + e.getMessage());
        }
    }
}

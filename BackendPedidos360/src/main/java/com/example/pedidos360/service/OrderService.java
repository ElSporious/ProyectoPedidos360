package com.example.pedidos360.service;

import com.example.pedidos360.dto.OrderItemRequest;
import com.example.pedidos360.dto.OrderRequest;
import com.example.pedidos360.dto.PedidoEvento;
import com.example.pedidos360.messaging.producer.PedidoEventoProducer;
import com.example.pedidos360.model.Order;
import com.example.pedidos360.model.OrderItem;
import com.example.pedidos360.model.OrderStatus;
import com.example.pedidos360.model.Product;
import com.example.pedidos360.repository.OrderRepository;
import com.example.pedidos360.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PedidoEventoProducer pedidoEventoProducer;

    @Transactional
    public Order createOrder(OrderRequest request, String customerEmail) {
        Order order = new Order();
        order.setCustomerEmail(customerEmail);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.CREADO);

        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.quantity());
            item.setPrice(product.getPrice());

            items.add(item);
        }
        order.setItems(items);
        Order savedOrder = orderRepository.save(order);

        PedidoEvento evento = new PedidoEvento(
                savedOrder.getId(),
                savedOrder.getStatus().name(),
                savedOrder.getCustomerEmail(),
                LocalDateTime.now()
        );
        pedidoEventoProducer.publicar("pedido.creado", evento);

        return savedOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByCustomer(String email) {
        return orderRepository.findByCustomerEmail(email);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);

        if (newStatus == OrderStatus.DESPACHADO && order.getStatus() == OrderStatus.CREADO) {
            throw new RuntimeException("No se puede despachar un pedido sin haber sido aceptado.");
        }

        if (newStatus == OrderStatus.ACEPTADO && order.getStatus() == OrderStatus.CREADO) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product.getStock() < item.getQuantity()) {
                    throw new RuntimeException("Stock insuficiente para el producto: " + product.getName());
                }
                product.setStock(product.getStock() - item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        // 🚀 PUBLICAR EVENTO SEGÚN EL NUEVO ESTADO (pedido.aceptado / pedido.despachado)
        String routingKey = switch (newStatus) {
            case ACEPTADO -> "pedido.aceptado";
            case DESPACHADO -> "pedido.despachado";
            default -> "pedido.actualizado";
        };

        PedidoEvento evento = new PedidoEvento(
                updatedOrder.getId(),
                updatedOrder.getStatus().name(),
                updatedOrder.getCustomerEmail(),
                LocalDateTime.now()
        );
        pedidoEventoProducer.publicar(routingKey, evento);

        return updatedOrder;
    }
}
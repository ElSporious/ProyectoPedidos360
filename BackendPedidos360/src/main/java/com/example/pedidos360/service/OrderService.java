package com.example.pedidos360.service;

import com.example.pedidos360.dto.OrderRequest;
import com.example.pedidos360.dto.OrderItemRequest;
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

    @Transactional
    public Order createOrder(OrderRequest request, String customerEmail) {
        Order order = new Order();
        order.setCustomerEmail(customerEmail);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.CREADO); // Estado inicial por defecto

        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.quantity());
            item.setPrice(product.getPrice()); // Congela el precio al momento del pedido

            items.add(item);
        }
        order.setItems(items);
        return orderRepository.save(order);
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

        // Regla de negocio: No se puede DESPACHAR sin haber sido ACEPTADO antes
        if (newStatus == OrderStatus.DESPACHADO && order.getStatus() == OrderStatus.CREADO) {
            throw new RuntimeException("No se puede despachar un pedido sin haber sido aceptado.");
        }

        // Regla de negocio: Al ACEPTAR un pedido, el stock debe disminuir
        if (newStatus == OrderStatus.ACEPTADO && order.getStatus() == OrderStatus.CREADO) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product.getStock() < item.getQuantity()) {
                    throw new RuntimeException("Stock insuficiente para el producto: " + product.getName());
                }
                product.setStock(product.getStock() - item.getQuantity());
                productRepository.save(product); // Actualiza el stock decrecido
            }
        }

        order.setStatus(newStatus);
        return orderRepository.save(order);
    }
}

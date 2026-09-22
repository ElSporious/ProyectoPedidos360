package com.example.pedidos360.repository;

import com.example.pedidos360.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Método útil para que un cliente vea solo sus propios pedidos
    List<Order> findByCustomerEmail(String customerEmail);
}
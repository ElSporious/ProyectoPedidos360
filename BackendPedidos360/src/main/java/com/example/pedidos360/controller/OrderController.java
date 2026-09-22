package com.example.pedidos360.controller;

import com.example.pedidos360.dto.OrderRequest;
import com.example.pedidos360.dto.OrderStatusRequest;
import com.example.pedidos360.model.Order;
import com.example.pedidos360.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('Cliente', 'Operador')")
    public Order createOrder(@RequestBody OrderRequest request, @AuthenticationPrincipal Jwt jwt) {
        // Extraemos el correo electrónico del token de Azure AD (suele venir en "preferred_username" o "upn")
        String email = jwt.getClaimAsString("preferred_username");
        if (email == null) {
            email = jwt.getClaimAsString("upn");
        }
        return orderService.createOrder(request, email);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'Operador', 'Cliente')")
    public List<Order> getOrders(@AuthenticationPrincipal Jwt jwt) {
        Collection<String> roles = jwt.getClaimAsStringList("roles");

        // Si es Admin u Operador, puede ver todos los pedidos
        if (roles != null && (roles.contains("Admin") || roles.contains("Operador"))) {
            return orderService.getAllOrders();
        }

        // Si es Cliente, solo ve sus propios pedidos[cite: 1]
        String email = jwt.getClaimAsString("preferred_username");
        if (email == null) {
            email = jwt.getClaimAsString("upn");
        }
        return orderService.getOrdersByCustomer(email);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin', 'Operador', 'Cliente')")
    public Order getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('Operador')") // Solo el operador puede cambiar el estado operacional[cite: 1]
    public Order updateOrderStatus(@PathVariable Long id, @RequestBody OrderStatusRequest request) {
        return orderService.updateOrderStatus(id, request.status());
    }
}

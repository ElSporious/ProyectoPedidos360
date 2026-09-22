package com.example.pedidos360.dto;

import java.util.List;

public record OrderRequest(List<OrderItemRequest> items) {

}
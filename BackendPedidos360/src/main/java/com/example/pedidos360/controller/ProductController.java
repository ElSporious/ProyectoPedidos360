package com.example.pedidos360.controller;

import com.example.pedidos360.model.Product;
import com.example.pedidos360.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasAnyRole('Admin', 'Operador', 'Cliente')")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public Product createProduct(@RequestBody Product product) {
        return productService.createProduct(product);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }
}

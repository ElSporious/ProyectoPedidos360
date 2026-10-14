package com.example.adminrabbit.controller;

import com.example.adminrabbit.dto.BindingRequest;
import com.example.adminrabbit.dto.ExchangeRequest;
import com.example.adminrabbit.dto.QueueRequest;
import com.example.adminrabbit.service.RabbitAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class RabbitAdminController {

    private final RabbitAdminService rabbitAdminService;

    @PostMapping("/queues")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('Admin')")
    public QueueRequest createQueue(@Valid @RequestBody QueueRequest request) {
        rabbitAdminService.createQueue(request);
        return request;
    }

    @GetMapping("/queues/{name}")
    @PreAuthorize("hasRole('Admin')")
    public Map<String, Object> getQueue(@PathVariable String name) {
        return rabbitAdminService.getQueue(name);
    }

    @DeleteMapping("/queues/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('Admin')")
    public void deleteQueue(@PathVariable String name) {
        rabbitAdminService.deleteQueue(name);
    }

    @PostMapping("/exchanges")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('Admin')")
    public ExchangeRequest createExchange(@Valid @RequestBody ExchangeRequest request) {
        rabbitAdminService.createExchange(request);
        return request;
    }

    @DeleteMapping("/exchanges/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('Admin')")
    public void deleteExchange(@PathVariable String name) {
        rabbitAdminService.deleteExchange(name);
    }

    @PostMapping("/bindings")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('Admin')")
    public BindingRequest createBinding(@Valid @RequestBody BindingRequest request) {
        rabbitAdminService.createBinding(request);
        return request;
    }

    @DeleteMapping("/bindings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('Admin')")
    public void deleteBinding(@Valid @RequestBody BindingRequest request) {
        rabbitAdminService.deleteBinding(request);
    }
}

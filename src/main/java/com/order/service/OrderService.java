package com.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order.dto.CreateOrderRequest;
import com.order.enums.OrderStatus;
import com.order.exception.OrderNotFoundException;
import com.order.model.Order;
import com.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository repository;
    private final ObjectMapper objectMapper;

    public OrderService(OrderRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public Order createOrder(CreateOrderRequest request) {
        logger.debug("Creating order for customer: {}", request.getCustomerId());
        
        Order order = new Order(
            UUID.randomUUID().toString(),
            request.getCustomerId(),
            request.getProduct(),
            request.getAmount(),
            OrderStatus.CREATED
        );

        repository.save(order);
        logger.info("Order created successfully | OrderId={} | CustomerId={} | Amount={}", 
                order.getOrderId(), order.getCustomerId(), order.getAmount());

        // BONUS: write file
        writeOrderToFile(order);

        return order;
    }

    public Order getOrder(String orderId) {
        logger.debug("Fetching order with id: {}", orderId);
        return repository.findById(orderId)
                .orElseThrow(() -> {
                    logger.warn("Order not found with id: {}", orderId);
                    return new OrderNotFoundException(orderId);
                });
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        logger.debug("Fetching orders for customer: {}", customerId);
        List<Order> orders = repository.findByCustomerId(customerId);
        logger.info("Found {} orders for customer: {}", orders.size(), customerId);
        return orders;
    }

    private void writeOrderToFile(Order order) {
        try {
            Path dir = Paths.get("input/orders");
            Files.createDirectories(dir);

            Path file = dir.resolve("order-" + order.getOrderId() + ".json");
            objectMapper.writeValue(file.toFile(), order);
            logger.debug("Order file written successfully: {}", file);
        } catch (Exception e) {
            logger.error("Failed to write order file for orderId: {}", order.getOrderId(), e);
            throw new RuntimeException("Failed to write order file", e);
        }
    }
}


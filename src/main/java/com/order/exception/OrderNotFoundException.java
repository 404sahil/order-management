package com.order.exception;

/**
 * Exception thrown when an order is not found.
 */
public class OrderNotFoundException extends RuntimeException {
    
    public OrderNotFoundException(String orderId) {
        super("Order not found with id: " + orderId);
    }
}


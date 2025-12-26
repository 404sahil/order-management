package com.order.model;

import java.io.Serializable;

import com.order.enums.OrderStatus;

/**
 * Order model class representing an order entity.
 */
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;
    private String orderId;
    private String customerId;
    private String product;
    private double amount;
    private OrderStatus status;

    public Order() {
    }

    public Order(String orderId, String customerId, String product, double amount, OrderStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.product = product;
        this.amount = amount;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}


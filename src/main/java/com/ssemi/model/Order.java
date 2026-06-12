package com.ssemi.model;

public class Order {
    private String orderId;
    private String sampleId;
    private String customerName;
    private int quantity;
    private OrderStatus status;

    public Order() {}

    public Order(String orderId, String sampleId, String customerName, int quantity, OrderStatus status) {
        this.orderId = orderId;
        this.sampleId = sampleId;
        this.customerName = customerName;
        this.quantity = quantity;
        this.status = status;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getSampleId() { return sampleId; }
    public void setSampleId(String sampleId) { this.sampleId = sampleId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Order{orderId='" + orderId + "', sampleId='" + sampleId + "', customerName='"
                + customerName + "', quantity=" + quantity + ", status=" + status + "}";
    }
}

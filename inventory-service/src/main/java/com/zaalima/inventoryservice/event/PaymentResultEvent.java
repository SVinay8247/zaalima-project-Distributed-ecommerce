package com.zaalima.inventoryservice.event;

public class PaymentResultEvent {

    private Long orderId;
    private Long productId;
    private Integer quantity;
    private String status;

    public PaymentResultEvent() {
    }

    public PaymentResultEvent(Long orderId, Long productId, Integer quantity, String status) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

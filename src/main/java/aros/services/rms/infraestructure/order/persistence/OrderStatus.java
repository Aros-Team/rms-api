package aros.services.rms.infraestructure.order.persistence;

public enum OrderStatus {
    CANCELLED,
    QUEUE,
    PREPARING,
    READY,
    DELIVERED
}
package ru.the_victor2009.onlinestore.entity;

public enum OrderStatus {
	PENDING,        // Ожидает обработки
    PROCESSING,     // В обработке
    SHIPPED,        // Отправлен
    DELIVERED,      // Доставлен
    CANCELLED,      // Отменен
    REFUNDED        // Возврат

}

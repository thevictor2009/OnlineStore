package ru.the_victor2009.onlinestore.service;

import ru.the_victor2009.onlinestore.entity.*;
import ru.the_victor2009.onlinestore.repository.OrderRepository;
import ru.the_victor2009.onlinestore.repository.ProductRepository;
import ru.the_victor2009.onlinestore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OrderService {
	 @Autowired
	    private OrderRepository orderRepository;
	    
	    @Autowired
	    private UserRepository userRepository;
	    
	    @Autowired
	    private ProductRepository productRepository;
	    
	    @Autowired
	    private ProductService productService;
	    
	    // 1. Получение всех заказов
	    public List<Order> getAllOrders() {
	        return orderRepository.findAll();
	    }
	    
	    // 2. Получение заказа по ID
	    public Order getOrderById(Long id) {
	        return orderRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Заказ с ID " + id + " не найден"));
	    }
	    
	    // 3. Получение заказов пользователя
	    public List<Order> getUserOrders(Long userId) {
	        AppUser user = userRepository.findById(userId)
	            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
	        return orderRepository.findByAppUser(user);
	    }
	    
	    // 4. Создание нового заказа
	    public Order createOrder(Long userId, List<OrderItem> items, 
	                            String shippingAddress, String customerNotes) {
	        AppUser user = userRepository.findById(userId)
	            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
	        
	        // Создаем заказ
	        Order order = new Order();
	        order.setAppUser(user);
	        order.setStatus(OrderStatus.PENDING);
	        order.setShippingAddress(shippingAddress != null ? shippingAddress : user.getAddress());
	        order.setBillingAddress(user.getAddress());
	        order.setPhoneNumber(user.getPhoneNumber());
	        order.setCustomerNotes(customerNotes);
	        
	        // Добавляем товары
	        for (OrderItem item : items) {
	            validateOrderItem(item);
	            
	            // Устанавливаем связь
	            item.setOrder(order);
	            order.getItems().add(item);
	            
	            // Резервируем товар на складе
	            productService.decreaseProductQuantity(item.getProduct().getId(), item.getQuantity());
	        }
	        
	        // Рассчитываем итоговую сумму
	        order.calculateTotalAmount();
	        
	        return orderRepository.save(order);
	    }
	    
	    // 5. Обновление статуса заказа
	    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
	        Order order = getOrderById(orderId);
	        
	        // Если заказ отменяется - возвращаем товары на склад
	        if (newStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
	            returnItemsToStock(order);
	        }
	        
	        order.setStatus(newStatus);
	        return orderRepository.save(order);
	    }
	    
	    // 6. Добавление товара в существующий заказ
	    public Order addItemToOrder(Long orderId, Long productId, Integer quantity) {
	        Order order = getOrderById(orderId);
	        
	        // Проверяем, можно ли изменить заказ
	        if (order.getStatus() != OrderStatus.PENDING) {
	            throw new RuntimeException("Невозможно изменить заказ в статусе: " + order.getStatus());
	        }
	        
	        Product product = productRepository.findById(productId)
	            .orElseThrow(() -> new RuntimeException("Товар не найден"));
	        
	        // Создаем позицию заказа
	        OrderItem item = new OrderItem();
	        item.setOrder(order);
	        item.setProduct(product);
	        item.setQuantity(quantity);
	        item.setPrice(product.getPrice());
	        
	        order.getItems().add(item);
	        
	        // Резервируем товар
	        productService.decreaseProductQuantity(productId, quantity);
	        
	        // Пересчитываем сумму
	        order.calculateTotalAmount();
	        
	        return orderRepository.save(order);
	    }
	    
	    // 7. Удаление заказа
	    public void deleteOrder(Long orderId) {
	        Order order = getOrderById(orderId);
	        
	        // Если заказ не отменен - возвращаем товары
	        if (order.getStatus() != OrderStatus.CANCELLED) {
	            returnItemsToStock(order);
	        }
	        
	        orderRepository.delete(order);
	    }
	    
	    // 8. Получение заказов по статусу
	    public List<Order> getOrdersByStatus(OrderStatus status) {
	        return orderRepository.findByStatus(status);
	    }
	    
	    // 9. Валидация позиции заказа
	    private void validateOrderItem(OrderItem item) {
	        if (item.getProduct() == null || item.getProduct().getId() == null) {
	            throw new RuntimeException("Товар не указан");
	        }
	        
	        if (item.getQuantity() == null || item.getQuantity() <= 0) {
	            throw new RuntimeException("Количество должно быть больше 0");
	        }
	        
	        // Проверяем наличие товара на складе
	        Product product = productRepository.findById(item.getProduct().getId())
	            .orElseThrow(() -> new RuntimeException("Товар не найден"));
	            
	        if (product.getQuantity() < item.getQuantity()) {
	            throw new RuntimeException("Недостаточно товара на складе: " + product.getName());
	        }
	    }
	    
	    // 10. Возврат товаров на склад
	    private void returnItemsToStock(Order order) {
	        for (OrderItem item : order.getItems()) {
	            productService.increaseProductQuantity(
	                item.getProduct().getId(), 
	                item.getQuantity()
	            );
	        }
	    }
	    
	    // 11. Получение общей выручки
	    public BigDecimal getTotalRevenue() {
	        List<Order> deliveredOrders = orderRepository.findByStatus(OrderStatus.DELIVERED);
	        
	        return deliveredOrders.stream()
	            .map(Order::getTotalAmount)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);
	    }
	    
	    // 12. Получение заказа по номеру
	    public Order getOrderByNumber(String orderNumber) {
	        return orderRepository.findByOrderNumber(orderNumber)
	            .orElseThrow(() -> new RuntimeException("Заказ не найден"));
	    }
	    
	    public Order cancelOrder(Long orderId, Long userId) {
	        Order order = getOrderById(orderId);
	        
	        // Проверяем, что заказ принадлежит пользователю
	        if (!order.getAppUser().getId().equals(userId)) {
	            throw new RuntimeException("Вы не можете отменить чужой заказ");
	        }
	        
	        // Проверяем, что заказ можно отменить
	        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.PROCESSING) {
	            throw new RuntimeException("Заказ уже " + getStatusDescription(order.getStatus()) + ". Отмена невозможна.");
	        }
	        
	        // Меняем статус
	        order.setStatus(OrderStatus.CANCELLED);
	        
	        // Возвращаем товары на склад
	        returnItemsToStock(order);
	        
	        return orderRepository.save(order);
	    }

	    private String getStatusDescription(OrderStatus status) {
	        return switch (status) {
	            case PENDING -> "ожидает обработки";
	            case PROCESSING -> "в обработке";
	            case SHIPPED -> "отправлен";
	            case DELIVERED -> "доставлен";
	            case CANCELLED -> "отменен";
	            case REFUNDED -> "возвращен";
	        };
	    }

}

package ru.the_victor2009.onlinestore.controller;

import ru.the_victor2009.onlinestore.entity.AppUser;
import ru.the_victor2009.onlinestore.entity.Order;
import ru.the_victor2009.onlinestore.repository.OrderRepository;
import ru.the_victor2009.onlinestore.repository.ProductRepository;
import ru.the_victor2009.onlinestore.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test") 
public class TestController {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    // ТЕСТЫ
    
    @GetMapping("/status")
    public String getStatus() {
        return "✅ Приложение работает!";
    }
    
    @GetMapping("/stats")
    public String getStats() {
        long products = productRepository.count();
        long users = userRepository.count();
        long orders = orderRepository.count();
        
        return String.format(
            "📊 Статистика:%n" +
            "   Товаров: %d%n" +
            "   Пользователей: %d%n" +
            "   Заказов: %d",
            products, users, orders);
    }
    
    // Методы для проверки связей
    
    @GetMapping("/user/{id}/orders")
    public List<Order> getUserOrders(@PathVariable Long id) {
        AppUser user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return orderRepository.findByAppUser(user);
    }
    
    @GetMapping("/order/{id}/user")
    public String getOrderUserInfo(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        
        if (order.getAppUser() != null) {
            return "Заказ #" + order.getOrderNumber() + 
                   " принадлежит пользователю: " + order.getAppUser().getUsername() +
                   " (" + order.getAppUser().getEmail() + ")";
        } else {
            return "У заказа #" + order.getOrderNumber() + " нет пользователя";
        }
    }
}
package ru.the_victor2009.onlinestore.controller;

import ru.the_victor2009.onlinestore.entity.Order;
import ru.the_victor2009.onlinestore.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Заказ не найден"));
    }
    
    @GetMapping("/count")
    public String getOrdersCount() {
        long count = orderRepository.count();
        return "Всего заказов: " + count;
    }
}

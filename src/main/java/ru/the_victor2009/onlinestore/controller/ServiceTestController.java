package ru.the_victor2009.onlinestore.controller;

import ru.the_victor2009.onlinestore.entity.Order;
import ru.the_victor2009.onlinestore.entity.OrderStatus;
import ru.the_victor2009.onlinestore.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/service-test")

public class ServiceTestController {
	
	  @Autowired
	    private ProductService productService;
	    
	    @Autowired
	    private UserService userService;
	    
	    @Autowired
	    private OrderService orderService;
	    
	    @Autowired
	    private CartService cartService;
	    
	    @GetMapping("/test-products")
	    public String testProductService() {
	        long count = productService.getAllProducts().size();
	        return "ProductService работает. Товаров в БД: " + count;
	    }
	    
	    @GetMapping("/test-users")
	    public String testUserService() {
	        long count = userService.getAllUsers().size();
	        return "UserService работает. Пользователей в БД: " + count;
	    }
	    
	    @GetMapping("/test-orders")
	    public String testOrderService() {
	    	try {
	            long count = orderService.getAllOrders().size();
	            BigDecimal revenue = orderService.getTotalRevenue();
	            
	            // Проверим статусы всех заказов
	            List<Order> allOrders = orderService.getAllOrders();
	            Map<OrderStatus, Long> statusCount = allOrders.stream()
	                .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
	            
	            return String.format(
	                "   OrderService работает.%n" +
	                "   Всего заказов: %d%n" +
	                "   Выручка : %.2f руб.%n" +
	                "   Статусы: %s",
	                count, revenue, statusCount);
	                
	        } catch (Exception e) {
	            return "❌ Ошибка в OrderService: " + e.getMessage() + 
	                   "\nТип ошибки: " + e.getClass().getSimpleName();
	        }
	    }
	    
	    @GetMapping("/test-cart/{userId}")
	    public String testCartService(@PathVariable Long userId) {
	        int itemCount = cartService.getCartItemCount(userId);
	        BigDecimal total = cartService.getCartTotal(userId);
	        
	        return "CartService работает. Товаров в корзине: " + itemCount + 
	               ", Сумма: " + total + " руб.";
	    }
	    
	    @PostMapping("/cart/add/{userId}/{productId}/{quantity}")
	    public String addToCartTest(
	            @PathVariable Long userId,
	            @PathVariable Long productId,
	            @PathVariable Integer quantity) {
	        cartService.addToCart(userId, productId, quantity);
	        return "Товар добавлен в корзину";
	    }

}

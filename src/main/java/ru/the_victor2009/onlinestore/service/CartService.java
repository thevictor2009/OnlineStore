package ru.the_victor2009.onlinestore.service;

import ru.the_victor2009.onlinestore.entity.Product;
import ru.the_victor2009.onlinestore.entity.AppUser;
import ru.the_victor2009.onlinestore.entity.Order;
import ru.the_victor2009.onlinestore.entity.OrderItem;
import ru.the_victor2009.onlinestore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderService orderService;
    
    // Хранилище корзин (временное, в памяти)
    private Map<Long, Map<Long, Integer>> userCarts = new HashMap<>();
    
    // 1. Добавление товара в корзину
    public void addToCart(Long userId, Long productId, Integer quantity) {
        validateQuantity(quantity);
        
        // Получаем или создаем корзину пользователя
        Map<Long, Integer> cart = userCarts.getOrDefault(userId, new HashMap<>());
        
        // Добавляем товар
        cart.put(productId, cart.getOrDefault(productId, 0) + quantity);
        
        // Сохраняем корзину
        userCarts.put(userId, cart);
    }
    
    // 2. Удаление товара из корзины
    public void removeFromCart(Long userId, Long productId) {
        Map<Long, Integer> cart = userCarts.get(userId);
        if (cart != null) {
            cart.remove(productId);
        }
    }
    
    // 3. Изменение количества товара в корзине
    public void updateCartItem(Long userId, Long productId, Integer quantity) {
        validateQuantity(quantity);
        
        Map<Long, Integer> cart = userCarts.get(userId);
        if (cart != null && cart.containsKey(productId)) {
            cart.put(productId, quantity);
        }
    }
    
    // 4. Получение содержимого корзины
    public List<CartItem> getCartContents(Long userId) {
        Map<Long, Integer> cart = userCarts.get(userId);
        List<CartItem> cartItems = new ArrayList<>();
        
        if (cart != null) {
            for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
                Product product = productRepository.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Товар не найден"));
                
                CartItem cartItem = new CartItem();
                cartItem.setProduct(product);
                cartItem.setQuantity(entry.getValue());
                cartItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
                
                cartItems.add(cartItem);
            }
        }
        
        return cartItems;
    }
    
    // 5. Очистка корзины
    public void clearCart(Long userId) {
        userCarts.remove(userId);
    }
    
    // 6. Получение суммы корзины
    public BigDecimal getCartTotal(Long userId) {
        List<CartItem> items = getCartContents(userId);
        
        return items.stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // 7. Оформление заказа из корзины
    public Order checkout(Long userId, String shippingAddress, String notes) {
        List<CartItem> cartItems = getCartContents(userId);
        
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Корзина пуста");
        }
        
        // Преобразуем CartItem в OrderItem
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItems.add(orderItem);
        }
        
        // Создаем заказ
        Order order = orderService.createOrder(userId, orderItems, shippingAddress, notes);
        
        // Очищаем корзину
        clearCart(userId);
        
        return order;
    }
    
    // 8. Получение количества товаров в корзине
    public int getCartItemCount(Long userId) {
        Map<Long, Integer> cart = userCarts.get(userId);
        if (cart == null) {
            return 0;
        }
        
        return cart.values().stream()
            .mapToInt(Integer::intValue)
            .sum();
    }
    
    // 9. Валидация количества
    private void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("Количество должно быть положительным числом");
        }
    }
    
    // Внутренний класс для представления товара в корзине
    public static class CartItem {
        private Product product;
        private Integer quantity;
        private BigDecimal subtotal;
        
        // Геттеры и сеттеры
        public Product getProduct() { return product; }
        public void setProduct(Product product) { this.product = product; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }
}
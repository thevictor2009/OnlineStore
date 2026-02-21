package ru.the_victor2009.onlinestore.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Order {
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;
	    
	    @ManyToOne
	    @JoinColumn(name = "user_id", nullable = false)
	    private AppUser appUser;
	    
	    @Column(name = "order_number", unique = true, length = 50)
	    private String orderNumber;
	    
	    @Enumerated(EnumType.STRING)
	    @Column(nullable = false)
	    @Builder.Default
	    private OrderStatus status = OrderStatus.PENDING;
	    
	    @Column(name = "total_amount", precision = 10, scale = 2)
	    private BigDecimal totalAmount;
	    
	    @Column(name = "shipping_address", length = 500)
	    private String shippingAddress;
	    
	    @Column(name = "billing_address", length = 500)
	    private String billingAddress;
	    
	    @Column(name = "phone_number", length = 20)
	    private String phoneNumber;
	    
	    @Column(name = "customer_notes", length = 1000)
	    private String customerNotes;
	    
	    @Column(name = "created_at")
	    private LocalDateTime createdAt;
	    
	    @Column(name = "updated_at")
	    private LocalDateTime updatedAt;
	    
	    // Связь: один заказ -> много позиций
	    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	    @Builder.Default
	    private List<OrderItem> items = new ArrayList<>();
	    
	    @PrePersist
	    protected void onCreate() {
	        createdAt = LocalDateTime.now();
	        updatedAt = LocalDateTime.now();
	        // Генерируем номер заказа
	        if (orderNumber == null) {
	            orderNumber = "ORD-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
	        }
	    }
	    
	    @PreUpdate
	    protected void onUpdate() {
	        updatedAt = LocalDateTime.now();
	    }
	    
	    // Метод для расчета общей суммы заказа
	    public void calculateTotalAmount() {
	        if (items != null && !items.isEmpty()) {
	            this.totalAmount = items.stream()
	                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
	                .reduce(BigDecimal.ZERO, BigDecimal::add);
	        } else {
	            this.totalAmount = BigDecimal.ZERO;
	        }
	    }

}

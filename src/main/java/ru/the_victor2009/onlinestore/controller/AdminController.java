package ru.the_victor2009.onlinestore.controller;

import ru.the_victor2009.onlinestore.entity.*;
import ru.the_victor2009.onlinestore.repository.CategoryRepository;
import ru.the_victor2009.onlinestore.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    // Главная страница админки
    @GetMapping("")
    public String adminDashboard(Model model) {
        model.addAttribute("stats", getDashboardStats());
        return "admin/index";
    }
    
    // Управление товарами
    
    @GetMapping("/products")
    public String adminProducts(Model model) {
        List<Product> products = productService.getAllProducts();
        List<Category> categories = categoryRepository.findAll();
        
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        return "admin/products/list";
    }
    
    @GetMapping("/products/create")
    public String createProductForm(Model model) {
        List<Category> categories = categoryRepository.findAll();
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categories);
        return "admin/products/create";
    }
    
    @PostMapping("/products/create")
    public String createProduct(@ModelAttribute Product product, @RequestParam(required = false) Long categoryId) {
    	  if (categoryId != null) {
    	        Category category = categoryRepository.findById(categoryId)
    	            .orElse(null);
    	        product.setCategory(category);
    	    }
          productService.createProduct(product);
          return "redirect:/admin/products?success";
    }
    
    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        List<Category> categories = categoryRepository.findAll();
        
        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        return "admin/products/edit";
    }
    
    @PostMapping("/products/edit/{id}")
    public String updateProduct(@PathVariable Long id, 
                               @ModelAttribute Product product) {
        productService.updateProduct(id, product);
        return "redirect:/admin/products?success";
    }
    /*
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin/products?success";
    }*/
    @GetMapping("/products/{id}")
    public String adminProductDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "admin/products/detail";
    }
    
    // Управление пользователями
    
    @GetMapping("/users")
    public String adminUsers(Model model) {
        List<AppUser> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users/list";
    }
    
    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        AppUser user = userService.getUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("roles", UserRole.values());
        return "admin/users/edit";
    }
    
    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id,
                            @ModelAttribute AppUser userDetails) {
        userService.updateUser(id, userDetails);
        return "redirect:/admin/users?success";
    }
    
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users?success";
    }
    
    @PostMapping("/users/{id}/role")
    public String changeUserRole(@PathVariable Long id,
                                @RequestParam UserRole role) {
        userService.changeUserRole(id, role);
        return "redirect:/admin/users?success";
    }
    
    // Управление заказами
    
    @GetMapping("/orders")
    public String adminOrders(Model model,
                             @RequestParam(required = false) String status) {
        List<Order> orders;
        
        if (status != null && !status.isEmpty()) {
            orders = orderService.getOrdersByStatus(OrderStatus.valueOf(status));
        } else {
            orders = orderService.getAllOrders();
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("statuses", OrderStatus.values());
        return "admin/orders/list";
    }
    
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        return "admin/orders/detail";
    }
    
    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                   @RequestParam OrderStatus status) {
        orderService.updateOrderStatus(id, status);
        return "redirect:/admin/orders/" + id + "?success";
    }
    
    // Статистика
    
    @GetMapping("/stats")
    public String statistics(Model model) {
        model.addAttribute("stats", getDashboardStats());
        model.addAttribute("recentOrders", orderService.getAllOrders()
            .stream()
            .limit(10)
            .toList());
        
        // Статистика по категориям
        Map<String, Integer> categoryStats = new HashMap<>();
        List<Product> products = productService.getAllProducts();
        
        for (Product product : products) {
            if (product.getCategory() != null) {
                String categoryName = product.getCategory().getName();
                categoryStats.put(categoryName, 
                    categoryStats.getOrDefault(categoryName, 0) + 1);
            }
        }
        
        model.addAttribute("categoryStats", categoryStats);
        return "admin/stats";
    }
    
    // Вспомогательные методы
    
    private Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Основная статистика
        stats.put("totalProducts", productService.getAllProducts().size());
        stats.put("totalUsers", userService.getAllUsers().size());
        stats.put("totalOrders", orderService.getAllOrders().size());
        stats.put("totalRevenue", orderService.getTotalRevenue());
        
        // Статистика по заказам
        List<Order> orders = orderService.getAllOrders();
        long pendingOrders = orders.stream()
            .filter(o -> o.getStatus() == OrderStatus.PENDING)
            .count();
        long deliveredOrders = orders.stream()
            .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
            .count();
        
        stats.put("pendingOrders", pendingOrders);
        stats.put("deliveredOrders", deliveredOrders);
        
        // Товары с низким запасом
        List<Product> lowStockProducts = productService.getAllProducts()
            .stream()
            .filter(p -> p.getQuantity() < 10)
            .toList();
        stats.put("lowStockProducts", lowStockProducts.size());
        
        return stats;
    }
}
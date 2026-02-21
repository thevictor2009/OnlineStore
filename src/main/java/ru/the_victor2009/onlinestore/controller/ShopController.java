package ru.the_victor2009.onlinestore.controller;

import ru.the_victor2009.onlinestore.entity.Product;
import ru.the_victor2009.onlinestore.entity.AppUser;
import ru.the_victor2009.onlinestore.entity.Order;
import ru.the_victor2009.onlinestore.entity.OrderItem;
import ru.the_victor2009.onlinestore.entity.OrderStatus;
import ru.the_victor2009.onlinestore.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ShopController {
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CartService cartService;
    
    @ModelAttribute("cartItemCount")
    public int getCartItemCount() {
        Long userId = getCurrentUserId();
        if (userId != null) {
            try {
                return cartService.getCartItemCount(userId);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }
    // Главная страница
    @GetMapping("/")
    public String home(Model model) {
        // Получаем несколько товаров для главной страницы
        List<Product> featuredProducts = productService.getAllProducts()
            .stream()
            .limit(6)
            .toList();
        
        model.addAttribute("featuredProducts", featuredProducts);
        model.addAttribute("cartItemCount", getCartItemCount());
        return "index";
    }
    
    // Страница со всеми товарами
    @GetMapping("/products")
    public String products(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        model.addAttribute("cartItemCount", getCartItemCount());
        return "products/list";
    }
    
    // Страница товара
    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("cartItemCount", getCartItemCount());
        return "products/detail";
    }
    
    // Корзина
    @GetMapping("/cart")
    public String cart(Model model) {
        Long userId = getCurrentUserId();
        
        if (userId != null) {
        	try {
            List<CartService.CartItem> cartItems = cartService.getCartContents(userId);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("cartTotal", cartService.getCartTotal(userId));
        	} catch(Exception e) {
        		 // Если корзина пуста или ошибка - оставляем пустой список
                model.addAttribute("cartItems", new ArrayList<>());
                model.addAttribute("cartTotal", BigDecimal.ZERO);
        	}
        } else {
        	// Для неавторизованных пользователей
            model.addAttribute("cartItems", new ArrayList<>());
            model.addAttribute("cartTotal", BigDecimal.ZERO);
        }
        
        model.addAttribute("cartItemCount", getCartItemCount());
        return "cart/view";
    }
    
    // Добавление в корзину
    @PostMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable Long productId, 
                           @RequestParam(defaultValue = "1") Integer quantity) {
        Long userId = getCurrentUserId();
        
        if (userId != null) {
        	try {
            cartService.addToCart(userId, productId, quantity);
        	} catch(Exception e) {
        		 // Логируем ошибку, но не прерываем выполнение
                System.err.println("Ошибка добавления в корзину: " + e.getMessage());
        	}
        }
        
        return "redirect:/cart";
    }
    
    // Удаление из корзины
    @PostMapping("/cart/remove/{productId}")
    public String removeFromCart(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        
        if (userId != null) {
            cartService.removeFromCart(userId, productId);
        }
        
        return "redirect:/cart";
    }
    
    // Оформление заказа
    @PostMapping("/cart/checkout")
	public String checkout(@RequestParam String shippingAddress, @RequestParam(required = false) String notes,
			RedirectAttributes redirectAttributes) {
		Long userId = getCurrentUserId();

		if (userId == null) {
			return "redirect:/login";
		}

		try {
            // Получаем товары из корзины
			List<CartService.CartItem> cartItems = cartService.getCartContents(userId);

			if (cartItems == null || cartItems.isEmpty()) {
				redirectAttributes.addFlashAttribute("error", "Корзина пуста");
				return "redirect:/cart";
			}

            // Преобразуем CartItem в OrderItem
			List<OrderItem> orderItems = new ArrayList<>();
			for (CartService.CartItem cartItem : cartItems) {
				OrderItem orderItem = new OrderItem();
				orderItem.setProduct(cartItem.getProduct());
				orderItem.setQuantity(cartItem.getQuantity());
				orderItem.setPrice(cartItem.getProduct().getPrice());
				orderItems.add(orderItem);
			}

            // Создаем заказ
			Order order = orderService.createOrder(userId, orderItems, shippingAddress, notes);

            // Очищаем корзину
			cartService.clearCart(userId);

             // Добавляем сообщение об успехе
			redirectAttributes.addFlashAttribute("success", "Заказ #" + order.getOrderNumber() + " успешно оформлен!");

            // Перенаправляем на страницу заказа
			return "redirect:/orders/" + order.getId();

		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Ошибка оформления заказа: " + e.getMessage());
			return "redirect:/cart";
		}
	}
    
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            try {
                AppUser user = userService.getUserByUsername(auth.getName());
                return user != null ? user.getId() : null;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    
    // Мои заказы
    @GetMapping("/orders")
    public String orders(Model model) {
        Long userId = getCurrentUserId();
        
        if (userId != null) {
            try {
                List<Order> orders = orderService.getUserOrders(userId);
                model.addAttribute("orders", orders);
            } catch (Exception e) {
                model.addAttribute("error", "Ошибка загрузки заказов: " + e.getMessage());
            }
        } else {
            return "redirect:/login";
        }
        
        return "orders/list";
    }
    
    // Детали заказа
	@GetMapping("/orders/{id}")
	public String orderDetail(@PathVariable Long id, Model model) {
		Long userId = getCurrentUserId();

		if (userId == null) {
			return "redirect:/login";
		}
		try {
			Order order = orderService.getOrderById(id);
			model.addAttribute("order", order);
		} catch (Exception e) {
			model.addAttribute("error", "Заказ не найден: " + e.getMessage());
		}
		return "orders/detail";
	}
    
    @PostMapping("/orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Long userId = getCurrentUserId();
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            Order cancelledOrder = orderService.cancelOrder(id, userId);
            
            redirectAttributes.addFlashAttribute("success", 
                "Заказ #" + cancelledOrder.getOrderNumber() + " успешно отменен. " +
                "Товары возвращены на склад.");
            
            return "redirect:/orders/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Ошибка отмены заказа: " + e.getMessage());
            return "redirect:/orders/" + id;
        }
    }
    
    // Страница регистрации
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new AppUser());
        model.addAttribute("cartItemCount", getCartItemCount());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String register(@ModelAttribute AppUser user, Model model) {
        try {
            userService.createUser(user);
            return "redirect:/login?registered";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "auth/register";
        }
    }
    
    // Страница входа (используется Spring Security)
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("cartItemCount", getCartItemCount());
        return "auth/login";
    }
    
    // Профиль пользователя
    @GetMapping("/profile")
    public String profile(Model model) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return "redirect:/login";
        }
        
        if (userId != null) {
            AppUser user = userService.getUserById(userId);
            model.addAttribute("user", user);
         // Статистика пользователя
            List<Order> userOrders = orderService.getUserOrders(userId);
            model.addAttribute("orderCount", userOrders.size());
            
            // Общая сумма покупок
            BigDecimal totalSpent = userOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("totalSpent", totalSpent);
        }
        
        model.addAttribute("cartItemCount", getCartItemCount());
        return "auth/profile";
    }
    
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute AppUser userDetails,
                               @RequestParam(required = false) String newPassword,
                               Model model) {
        Long userId = getCurrentUserId();
        
        if (userId == null) {
            return "redirect:/login";
        }
        
        try {
            // Получаем текущего пользователя
            AppUser currentUser = userService.getUserById(userId);
            
            // Обновляем данные
            currentUser.setFirstName(userDetails.getFirstName());
            currentUser.setLastName(userDetails.getLastName());
            currentUser.setEmail(userDetails.getEmail());
            currentUser.setPhoneNumber(userDetails.getPhoneNumber());
            currentUser.setAddress(userDetails.getAddress());
            
            // Обновляем пароль, если указан новый
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                currentUser.setPassword(newPassword);
            }
            
            // Сохраняем изменения
            userService.updateUser(userId, currentUser);
            
            return "redirect:/profile?updated";
            
        } catch (Exception e) {
            model.addAttribute("error", "Ошибка обновления профиля: " + e.getMessage());
            model.addAttribute("user", userService.getUserById(userId));
            model.addAttribute("cartItemCount", getCartItemCount());
            return "auth/profile";
        }
    }
   
    @GetMapping("/logout")
    public String logout() {
        // Spring Security -выход автоматом
        return "redirect:/login?logout";
    }
}

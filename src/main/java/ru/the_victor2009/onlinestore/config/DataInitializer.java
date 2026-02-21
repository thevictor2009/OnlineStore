package ru.the_victor2009.onlinestore.config;

import ru.the_victor2009.onlinestore.entity.AppUser;
import ru.the_victor2009.onlinestore.entity.Category;
import ru.the_victor2009.onlinestore.entity.Order;
import ru.the_victor2009.onlinestore.entity.OrderItem;
import ru.the_victor2009.onlinestore.entity.OrderStatus;
import ru.the_victor2009.onlinestore.entity.Product;
import ru.the_victor2009.onlinestore.entity.UserRole;
import ru.the_victor2009.onlinestore.repository.CategoryRepository;
import ru.the_victor2009.onlinestore.repository.OrderRepository;
import ru.the_victor2009.onlinestore.repository.ProductRepository;
import ru.the_victor2009.onlinestore.repository.UserRepository;
import ru.the_victor2009.onlinestore.service.CartService;
import ru.the_victor2009.onlinestore.service.ProductService;
import ru.the_victor2009.onlinestore.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {
	 @Autowired
     private ProductService productService;
	 
	 @Autowired
	 private PasswordEncoder passwordEncoder;  

     @Autowired
     private CartService cartService;
	@Bean
	public CommandLineRunner initData(
			ProductRepository productRepository, 
			CategoryRepository categoryRepository,
			UserRepository userRepository,
			OrderRepository orderRepository,
			PasswordEncoder passwordEncoder,
			UserService userService) {//!!!!!!!!!!!!!!!!!!!!!!!!!!
		return args -> {
            // Создаем категории
            Category electronics = Category.builder().name("Электроника").description("Техника и вычислительные устройства").build();
            Category books = Category.builder().name("Книги").description("Книги и литература").build();
            
            //Сохраняем категории
            electronics = categoryRepository.save(electronics);
            books = categoryRepository.save(books);
            
            // 2. Создаем товары через Builder
            Product laptop = Product.builder()
                .name("Ноутбук Lenovo IdeaPad")
                .description("15.6\" IPS, Intel Core i5, 8GB RAM, 512GB SSD")
                .price(new BigDecimal("54999.99"))
                .quantity(15)
                .imageUrl("/images/laptop.jpg")
                .category(electronics)
                .build();
            Product smartphone = Product.builder()
                .name("Смартфон Samsung Galaxy S23")
                .description("6.1\" Dynamic AMOLED, 128GB, черный")
                .price(new BigDecimal("79999.50"))
                .quantity(8)
                .imageUrl("/images/smartphone.jpg")
                .category(electronics)
                .build();
            
            Product headphones = Product.builder()
                .name("Наушники Sony WH-1000XM4")
                .description("Беспроводные наушники с шумоподавлением")
                .price(new BigDecimal("23999.00"))
                .quantity(30)
                .imageUrl("/images/headphones.jpg")
                .category(electronics)
                .build();
            
            Product javaBook = Product.builder()
                .name("Java. Полное руководство")
                .description("11-е издание, Герберт Шилдт")
                .price(new BigDecimal("2499.99"))
                .quantity(50)
                .imageUrl("/images/java-book.jpg")
                .category(books)
                .build();
            
            Product springBook = Product.builder()
                .name("Spring в действии")
                .description("6-е издание, Крейг Уоллс")
                .price(new BigDecimal("3299.00"))
                .quantity(25)
                .imageUrl("/images/spring-book.jpg")
                .category(books)
                .build();
            
            // Сохраняем все товары
            productRepository.save(laptop);
            productRepository.save(smartphone);
            productRepository.save(headphones);
            productRepository.save(javaBook);
            productRepository.save(springBook);
            
            //Создаем пользователей
            AppUser admin = AppUser.builder()
            		.username("admin")
            		.email("admin@store.com")
            		.password(passwordEncoder.encode("admin123"))//хешируем пароль
            		.firstName("Алексей")
            		.lastName("Петров")
            		.phoneNumber("+79001234567")
            		.address("Москва, ул. Тверская, 1")
            		.role(UserRole.ROLE_ADMIN)
            		.build();
            AppUser customer = AppUser.builder()
					.username("victor")
					.email("the-victor2009@mail.ru")
					.password(passwordEncoder.encode("111111"))
					.firstName("Victor")
					.lastName("Zatylkin")
					.phoneNumber("89172183047")
					.address("Engels, Engels-1, 1/175")
					.role(UserRole.ROLE_CUSTOMER)
					.build();
            
            admin = userRepository.save(admin);
            customer = userRepository.save(customer);
            
            
            //Создаем заказы
            Order order = Order.builder()
            		.appUser(customer)
            		.status(OrderStatus.PROCESSING)
            		.shippingAddress(customer.getAddress())
            		.billingAddress(customer.getAddress())
            		.phoneNumber(customer.getPhoneNumber())
            		.customerNotes("Позвонить перед доставкой")
            		.build();
            
            //Создаем позиции заказа
            OrderItem item1 = OrderItem.builder()
            		.product(laptop)
            		.quantity(1)
            		.price(laptop.getPrice())
            		.build();
            OrderItem item2 = OrderItem.builder()
            		.product(javaBook)
            		.quantity(2)
            		.price(javaBook.getPrice())
            		.build();
            // Добавляем позиции в заказ
            order.getItems().add(item1);
            order.getItems().add(item2);

            // Устанавливаем связь с заказом для позиций
            item1.setOrder(order);
            item2.setOrder(order);
            
            // Рассчитываем общую сумму
            order.calculateTotalAmount();
            order= orderRepository.save(order);
            
            System.out.println(" Тестовые данные добавлены в БД");
            System.out.println(" Категорий: " + categoryRepository.count());
            System.out.println(" Товаров: " + productRepository.count());
            
            // Tест корзины:
            cartService.addToCart(customer.getId(), laptop.getId(), 1);
            cartService.addToCart(customer.getId(), javaBook.getId(), 2);

            System.out.println(" Тест корзины:");
            System.out.println("   Товаров в корзине: " + cartService.getCartItemCount(customer.getId()));
            System.out.println("   Сумма корзины: " + cartService.getCartTotal(customer.getId()) + " руб.");
            		
            
           
           

            
        };
		
	}
			

	

}

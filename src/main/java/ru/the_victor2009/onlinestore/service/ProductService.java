package ru.the_victor2009.onlinestore.service;

import ru.the_victor2009.onlinestore.entity.Product;
import ru.the_victor2009.onlinestore.entity.Category;
import ru.the_victor2009.onlinestore.repository.ProductRepository;
import ru.the_victor2009.onlinestore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {
	  @Autowired
	    private ProductRepository productRepository;
	    
	    @Autowired
	    private CategoryRepository categoryRepository;
	    
	    // 1. Получение всех товаров
	    public List<Product> getAllProducts() {
	        return productRepository.findAll();
	    }
	    
	    // 2. Получение товара по ID
	    public Product getProductById(Long id) {
	        return productRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Товар с ID " + id + " не найден"));
	    }
	    
	    // 3. Поиск товаров по названию
	    public List<Product> searchProductsByName(String name) {
	        return productRepository.findByNameContainingIgnoreCase(name);
	    }
	    
	    // 4. Фильтрация товаров по цене
	    public List<Product> filterProductsByPrice(BigDecimal minPrice, BigDecimal maxPrice) {
	        return productRepository.findByPriceBetween(minPrice, maxPrice);
	    }
	    
	    // 5. Получение товаров по категории
	    public List<Product> getProductsByCategory(Long categoryId) {
	        Category category = categoryRepository.findById(categoryId)
	            .orElseThrow(() -> new RuntimeException("Категория не найдена"));
	        return category.getProducts();
	    }
	    
	    // 6. Создание нового товара
	    public Product createProduct(Product product) {
	        validateProduct(product);
	        
	        if (product.getCategory() != null && product.getCategory().getId() != null) {
	            // Убедимся, что категория существует
	            Category category = categoryRepository.findById(product.getCategory().getId())
	                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
	            product.setCategory(category);
	        }
	        
	        return productRepository.save(product);
	    }
	    
	    // 7. Обновление товара
	    public Product updateProduct(Long id, Product productDetails) {
	        Product product = getProductById(id);
	        
	        product.setName(productDetails.getName());
	        product.setDescription(productDetails.getDescription());
	        product.setPrice(productDetails.getPrice());
	        product.setQuantity(productDetails.getQuantity());
	        product.setImageUrl(productDetails.getImageUrl());
	        
	        if (productDetails.getCategory() != null) {
	            product.setCategory(productDetails.getCategory());
	        }
	        
	        return productRepository.save(product);
	    }
	    
	    // 8. Удаление товара
	    public void deleteProduct(Long id) {
	        Product product = getProductById(id);
	        productRepository.delete(product);
	    }
	    
	    // 9. Уменьшение количества товара (при покупке)
	    public void decreaseProductQuantity(Long productId, Integer quantity) {
	        Product product = getProductById(productId);
	        
	        if (product.getQuantity() < quantity) {
	            throw new RuntimeException("Недостаточно товара на складе. Доступно: " + product.getQuantity());
	        }
	        
	        product.setQuantity(product.getQuantity() - quantity);
	        productRepository.save(product);
	    }
	    
	    // 10. Увеличение количества товара (при пополнении склада)
	    public void increaseProductQuantity(Long productId, Integer quantity) {
	        Product product = getProductById(productId);
	        product.setQuantity(product.getQuantity() + quantity);
	        productRepository.save(product);
	    }
	    
	    // 11. Валидация товара
	    private void validateProduct(Product product) {
	        if (product.getName() == null || product.getName().trim().isEmpty()) {
	            throw new RuntimeException("Название товара не может быть пустым");
	        }
	        
	        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
	            throw new RuntimeException("Цена товара должна быть положительной");
	        }
	        
	        if (product.getQuantity() == null || product.getQuantity() < 0) {
	            throw new RuntimeException("Количество товара не может быть отрицательным");
	        }
	    }
	    
	    // 12. Получение доступных товаров (quantity > 0)
	    public List<Product> getAvailableProducts() {
	        return productRepository.findByQuantityGreaterThan(0);
	    }
}

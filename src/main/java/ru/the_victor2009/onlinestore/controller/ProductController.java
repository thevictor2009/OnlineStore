package ru.the_victor2009.onlinestore.controller;

import ru.the_victor2009.onlinestore.entity.Category;
import ru.the_victor2009.onlinestore.entity.Product;
import ru.the_victor2009.onlinestore.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
	 @Autowired
	    private ProductService productService;
	    
	    // GET /api/products
	    @GetMapping
	    public List<Product> getAllProducts() {
	        return productService.getAllProducts();
	    }
	    
	    // GET /api/products/{id}
	    @GetMapping("/{id}")
	    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
	        Product product = productService.getProductById(id);
	        return ResponseEntity.ok(product);
	    }
	    
	    // GET /api/products/search?name=...
	    @GetMapping("/search")
	    public List<Product> searchProducts(@RequestParam String name) {
	        return productService.searchProductsByName(name);
	    }
	    
	    // GET /api/products/filter?minPrice=...&maxPrice=...
	    @GetMapping("/filter")
	    public List<Product> filterProducts(
	            @RequestParam BigDecimal minPrice,
	            @RequestParam BigDecimal maxPrice) {
	        return productService.filterProductsByPrice(minPrice, maxPrice);
	    }
	    
	    // GET /api/products/category/{categoryId}
	    @GetMapping("/category/{categoryId}")
	    public List<Product> getProductsByCategory(@PathVariable Long categoryId) {
	        return productService.getProductsByCategory(categoryId);
	    }
	    
	    // POST /api/products
	    @PostMapping
	    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
	        Product createdProduct = productService.createProduct(product);
	        return ResponseEntity.ok(createdProduct);
	    }
	    
	    // PUT /api/products/{id}
	    @PutMapping("/{id}")
	    public ResponseEntity<Product> updateProduct(
	            @PathVariable Long id,
	            @RequestBody Product productDetails) {
	        Product updatedProduct = productService.updateProduct(id, productDetails);
	        return ResponseEntity.ok(updatedProduct);
	    }
	    
	    // DELETE /api/products/{id}
	    @DeleteMapping("/{id}")
	    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
	        productService.deleteProduct(id);
	        return ResponseEntity.noContent().build();
	    }
	    
	    // GET /api/products/available
	    @GetMapping("/available")
	    public List<Product> getAvailableProducts() {
	        return productService.getAvailableProducts();
	    }
}

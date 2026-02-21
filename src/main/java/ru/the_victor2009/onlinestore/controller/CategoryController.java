package ru.the_victor2009.onlinestore.controller;

import ru.the_victor2009.onlinestore.entity.Category;
import ru.the_victor2009.onlinestore.entity.Product;
import ru.the_victor2009.onlinestore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/products/category")
public class CategoryController {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @GetMapping("/{id}")
    public String getProductsByCategory(@PathVariable Long id, Model model) {
        // Получаем категорию по ID
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        
        // Получаем товары этой категории
        List<Product> products = category.getProducts();
        
        // Добавляем в модель
        model.addAttribute("products", products);
        model.addAttribute("category", category);
        model.addAttribute("title", category.getName());
        
        // ИСПОЛЬЗУЕМ СУЩЕСТВУЮЩИЙ ШАБЛОН list.html
        return "products/list";
    }
}

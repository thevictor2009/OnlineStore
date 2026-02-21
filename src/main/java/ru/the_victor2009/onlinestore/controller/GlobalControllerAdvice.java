package ru.the_victor2009.onlinestore.controller;

import ru.the_victor2009.onlinestore.entity.Category;
import ru.the_victor2009.onlinestore.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @ModelAttribute("categories")
    public List<Category> addCategoriesToModel() {
        return categoryRepository.findAll();
    }
}
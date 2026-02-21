package ru.the_victor2009.onlinestore.repository;

import ru.the_victor2009.onlinestore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>{
	//Автоматически создаем запросы по имени кода Spring Data JPA
	List<Product> findByNameContainingIgnoreCase(String name);
	List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
	List<Product> findByQuantityGreaterThan(Integer quantity);

}

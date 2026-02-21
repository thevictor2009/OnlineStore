package ru.the_victor2009.onlinestore.repository;

import ru.the_victor2009.onlinestore.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	Category findByName(String name);

}

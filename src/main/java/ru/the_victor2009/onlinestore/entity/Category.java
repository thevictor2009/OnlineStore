package ru.the_victor2009.onlinestore.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, unique = true, length = 100)
	private String name;
	
	@Column(length = 500)
	private String description;
	
	//Связь: одна категория - много товаров
	@JsonIgnore//Убираем циклический вывод
	@OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
	@Builder.Default
	private List<Product> products = new ArrayList<>();
}

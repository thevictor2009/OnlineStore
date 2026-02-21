package ru.the_victor2009.onlinestore.repository;

import ru.the_victor2009.onlinestore.entity.Order;
import ru.the_victor2009.onlinestore.entity.OrderStatus;
import ru.the_victor2009.onlinestore.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>{
	 // Метод для поиска по пользователю
    List<Order> findByAppUser(AppUser appUser);
    
    // Метод для поиска по ID пользователя
    List<Order> findByAppUserId(Long userId);
    
    // ПРАВИЛЬНЫЙ способ: поиск по статусу (enum)
    List<Order> findByStatus(OrderStatus status);
    
    // Альтернативный способ с @Query (если нужно искать по строке)
    @Query("SELECT o FROM Order o WHERE o.status = :status")
    List<Order> findByStatusString(@Param("status") String status);
    
    // Поиск по номеру заказа
    Optional<Order> findByOrderNumber(String orderNumber);
    
    // Дополнительные методы для бизнес-логики
    List<Order> findByStatusIn(List<OrderStatus> statuses);
    
    // Поиск заказов с определенным минимальным итогом
    @Query("SELECT o FROM Order o WHERE o.totalAmount >= :minAmount")
    List<Order> findByTotalAmountGreaterThanEqual(@Param("minAmount") BigDecimal minAmount);

}

package ru.the_victor2009.onlinestore.service;

import ru.the_victor2009.onlinestore.entity.AppUser;
import ru.the_victor2009.onlinestore.entity.UserRole;
import ru.the_victor2009.onlinestore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
	  @Autowired
	    private UserRepository userRepository;
	    
	    @Autowired
	    private PasswordEncoder passwordEncoder;
	    
	    // 1. Получение всех пользователей
	    public List<AppUser> getAllUsers() {
	        return userRepository.findAll();
	    }
	    
	    // 2. Получение пользователя по ID
	    public AppUser getUserById(Long id) {
	        return userRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("Пользователь с ID " + id + " не найден"));
	    }
	    
	    // 3. Получение пользователя по username
	    public AppUser getUserByUsername(String username) {
	        return userRepository.findByUsername(username)
	            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
	    }
	    
	    // 4. Создание нового пользователя (регистрация)
	    public AppUser createUser(AppUser user) {
	        validateUser(user);
	        
	        // Проверка уникальности username и email
	        if (userRepository.existsByUsername(user.getUsername())) {
	            throw new RuntimeException("Пользователь с таким именем уже существует");
	        }
	        
	        if (userRepository.existsByEmail(user.getEmail())) {
	            throw new RuntimeException("Пользователь с таким email уже существует");
	        }
	        
	        // Хэширование пароля
	        user.setPassword(passwordEncoder.encode(user.getPassword()));
	        
	        // По умолчанию - обычный пользователь
	        if (user.getRole() == null) {
	            user.setRole(UserRole.ROLE_CUSTOMER);
	        }
	        
	        return userRepository.save(user);
	    }
	    
	    // 5. Обновление пользователя
	    public AppUser updateUser(Long id, AppUser userDetails) {
	        AppUser user = getUserById(id);
	        
	        user.setFirstName(userDetails.getFirstName());
	        user.setLastName(userDetails.getLastName());
	        user.setEmail(userDetails.getEmail());
	        user.setPhoneNumber(userDetails.getPhoneNumber());
	        user.setAddress(userDetails.getAddress());
	        
	        // Если пароль указан - обновляем его
	        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
	            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
	        }
	        
	        return userRepository.save(user);
	    }
	    
	    // 6. Удаление пользователя
	    public void deleteUser(Long id) {
	        AppUser user = getUserById(id);
	        userRepository.delete(user);
	    }
	    
	    // 7. Аутентификация пользователя
	    public boolean authenticate(String username, String password) {
	        Optional<AppUser> userOptional = userRepository.findByUsername(username);
	        
	        if (userOptional.isPresent()) {
	            AppUser user = userOptional.get();
	            return passwordEncoder.matches(password, user.getPassword());
	        }
	        
	        return false;
	    }
	    
	    // 8. Изменение роли пользователя
	    public AppUser changeUserRole(Long userId, UserRole newRole) {
	        AppUser user = getUserById(userId);
	        user.setRole(newRole);
	        return userRepository.save(user);
	    }
	    
	    // 9. Поиск пользователей по email
	    public AppUser findUserByEmail(String email) {
	        return userRepository.findByEmail(email)
	            .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
	    }
	    
	    // 10. Валидация пользователя
	    private void validateUser(AppUser user) {
	        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
	            throw new RuntimeException("Имя пользователя не может быть пустым");
	        }
	        
	        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
	            throw new RuntimeException("Email не может быть пустым");
	        }
	        
	        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
	            throw new RuntimeException("Пароль не может быть пустым");
	        }
	        
	        if (user.getPassword().length() < 6) {
	            throw new RuntimeException("Пароль должен содержать минимум 6 символов");
	        }
	    }

}

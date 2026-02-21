package ru.the_victor2009.onlinestore.controller;

import ru.the_victor2009.onlinestore.entity.AppUser;
import ru.the_victor2009.onlinestore.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
	@Autowired
    private UserService userService;
    
    // GET /api/users
    @GetMapping
    public List<AppUser> getAllUsers() {
        return userService.getAllUsers();
    }
    
    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AppUser> getUserById(@PathVariable Long id) {
        AppUser user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    // POST /api/users (регистрация)
    @PostMapping
    public ResponseEntity<AppUser> createUser(@RequestBody AppUser user) {
        AppUser createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }
    
    // PUT /api/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<AppUser> updateUser(
            @PathVariable Long id,
            @RequestBody AppUser userDetails) {
        AppUser updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(updatedUser);
    }
    
    // DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
    
    // POST /api/users/login (аутентификация)
    @PostMapping("/login")
    public ResponseEntity<String> login(
            @RequestParam String username,
            @RequestParam String password) {
        boolean authenticated = userService.authenticate(username, password);
        
        if (authenticated) {
            return ResponseEntity.ok("Аутентификация успешна");
        } else {
            return ResponseEntity.status(401).body("Неверные учетные данные");
        }
    }
}

package ru.the_victor2009.onlinestore.config;

import ru.the_victor2009.onlinestore.entity.AppUser;
import ru.the_victor2009.onlinestore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Ищем пользователя в БД
        AppUser appUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
        
        // Преобразуем роль в GrantedAuthority
        Collection<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority(appUser.getRole().name())
        );
        
        // Создаем UserDetails
        return new User(
            appUser.getUsername(),
            appUser.getPassword(),  // Пароль уже должен быть захеширован
            authorities
        );
    }
}
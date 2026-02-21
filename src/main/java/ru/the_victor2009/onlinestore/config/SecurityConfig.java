package ru.the_victor2009.onlinestore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	 private final UserDetailsService userDetailsService;
	    
	    public SecurityConfig(UserDetailsService userDetailsService) {
	        this.userDetailsService = userDetailsService;
	    }
	    
    // PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
 
    
    //  SecurityFilterChain
	  @Bean
	    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
			http
	        .authorizeHttpRequests(auth -> auth
	            // Публичные страницы
	        	//.requestMatchers("/api/products/**").permitAll()
	            .requestMatchers("/products/category/**").permitAll()
	            .requestMatchers("/", "/products", "/products/**", "/css/**", "/js/**", "/images/**").permitAll()
	            .requestMatchers("/register", "/login", "/h2-console/**").permitAll()
	           
	            
	            // Требуют аутентификации
	            .requestMatchers( "/cart/**", "/orders/**", "/profile").authenticated()
	            .requestMatchers("/orders/*/cancel").authenticated()

	            // Админка
	            .requestMatchers("/admin/**").hasRole("ADMIN")
	            
	            //  Все остальное требует входа
	            .anyRequest().authenticated()
	        )
	        .formLogin(form -> form
	            .loginPage("/login")
	            .defaultSuccessUrl("/")
	            .permitAll()
	        )
	        .logout(logout -> logout
	            .logoutSuccessUrl("/")
	            .permitAll()
	        )
	        .headers(headers -> headers
	            .frameOptions(frame -> frame.sameOrigin())
	        )
	        .csrf(csrf -> csrf
	                .ignoringRequestMatchers("/h2-console/**")  // Отключаем CSRF для H2
	            )
	        .userDetailsService(userDetailsService);  // Используем нашу реализацию	    
	    return http.build();
	    }
	
}
package org.example.bigdatahackathon.controller;

import org.example.bigdatahackathon.entity.User;
import org.example.bigdatahackathon.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    
    @Autowired
    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String email = request.get("email");
            
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Имя пользователя обязательно"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Пароль обязателен"));
            }
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email обязателен"));
            }
            
            User user = userService.registerUser(username, password, email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Пользователь успешно зарегистрирован");
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            if (username == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Имя пользователя и пароль обязательны"));
            }
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Успешный вход");
            response.put("username", username);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Неверное имя пользователя или пароль"));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || 
            "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("error", "Не авторизован"));
        }
        
        String username = authentication.getName();
        Map<String, Object> response = new HashMap<>();
        response.put("username", username);
        response.put("authorities", authentication.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Успешный выход"));
    }
}



package org.example.bigdatahackathon.service;

import org.example.bigdatahackathon.entity.User;
import org.example.bigdatahackathon.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public User registerUser(String username, String password, String email) {
        if (repository.existsByUsername(username)) {
            throw new RuntimeException("Пользователь с таким именем уже существует");
        }
        if (repository.existsByEmail(email)) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.getRoles().add(User.Role.USER);
        
        return repository.save(user);
    }
    
    public User findByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
    
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }
}



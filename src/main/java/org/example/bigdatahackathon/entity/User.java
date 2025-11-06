package org.example.bigdatahackathon.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String email;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "enabled")
    private Boolean enabled = true;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (roles.isEmpty()) {
            roles.add(Role.USER);
        }
    }
    
    public enum Role {
        USER, ADMIN
    }
}



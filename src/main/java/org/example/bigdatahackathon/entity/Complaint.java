package org.example.bigdatahackathon.entity;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "complaints", indexes = {
    @Index(name = "idx_complaints_route", columnList = "route"),
    @Index(name = "idx_complaints_priority", columnList = "priority"),
    @Index(name = "idx_complaints_actor", columnList = "actor"),
    @Index(name = "idx_complaints_place", columnList = "place")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;
    
    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;
    
    @Column(columnDefinition = "TEXT")
    private String route;
    
    @Column(columnDefinition = "TEXT")
    private String object;
    
    @Column(name = "time")
    private OffsetDateTime time;
    
    @Column(columnDefinition = "TEXT")
    private String place;
    
    @Column(columnDefinition = "TEXT")
    private String actor;
    
    @Type(ListArrayType.class)
    @Column(name = "aspect", columnDefinition = "TEXT[]")
    private List<String> aspect = new ArrayList<>();
    
    @Column(columnDefinition = "TEXT")
    private String priority;
    
    @Type(ListArrayType.class)
    @Column(name = "evidence", columnDefinition = "TEXT[]")
    private List<String> evidence = new ArrayList<>();
    
    @Column(columnDefinition = "DOUBLE PRECISION")
    private Double confidence;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
        if (aspect == null) {
            aspect = new ArrayList<>();
        }
        if (evidence == null) {
            evidence = new ArrayList<>();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}


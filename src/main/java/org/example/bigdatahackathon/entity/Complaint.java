package org.example.bigdatahackathon.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
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
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "aspect", columnDefinition = "text[]")
    private String[] aspect;
    
    @Column(columnDefinition = "TEXT")
    private String priority;
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "evidence", columnDefinition = "text[]")
    private String[] evidence;
    
    @Column(columnDefinition = "DOUBLE PRECISION")
    private Double confidence;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "user_id")
    private Long userId;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
        if (aspect == null) aspect = new String[]{};
        if (evidence == null) evidence = new String[]{};
        if (status == null) status = "NEW";
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}


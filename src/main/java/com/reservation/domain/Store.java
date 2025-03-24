package com.reservation.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "stores")
public class Store {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  

    @Column(name = "store_name", nullable = false, length = 20)
    private String storeName;  

    @Column(nullable = true)  
    private Double lat;

    @Column(nullable = true)  
    private Double lng;

    @Column(columnDefinition = "TEXT")
    private String detail;  

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; 

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;  

    // User의 id를 외래키 ownerId로 받아옴
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)  // 외래키 설정
    private User owner;
}

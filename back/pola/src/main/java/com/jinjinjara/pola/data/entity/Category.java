package com.jinjinjara.pola.data.entity;

import com.jinjinjara.pola.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "category_name", nullable = false)
    private String categoryName;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "file_count", nullable = false)
    private Integer fileCount;

    public void increaseCount(int delta) {
        this.fileCount += delta;
    }
    public void decreaseCount(int delta) {
        this.fileCount -= delta;
        if (this.fileCount < 0) this.fileCount = 0;
    }

    @PrePersist
    public void prePersist() {
        fileCount = 0;
    }
}

package com.jinjinjara.pola.entity;

import com.jinjinjara.pola.dto.common.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "Users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_users_google_sub", columnNames = "google_sub"),
                @UniqueConstraint(name = "uk_users_user_vector_id", columnNames = "user_vector_id")
        }
)
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_sub", nullable = false, length = 256)
    private String googleSub;

    @Column(name = "email", nullable = false, length = 256)
    private String email;

    @Column(name = "display_name", nullable = false, length = 256)
    private String displayName;

    @Column(name = "profile_image_url", length = 256)
    private String profileImageUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "favorite_sum", nullable = false)
    private int favoriteSum;

    @Column(name = "user_vector_id", length = 255)
    private String userVectorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private Role role;

    public void authorizeUser() {
        this.role = Role.ROLE_USER;
    }

    @PrePersist
    private void prePersist() {
        if (favoriteSum < 0) {
            favoriteSum = 0;
        }
    }
}
package com.jinjinjara.pola.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Files")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Integer categoryId;
    private String src;
    private String type;
    private LocalDateTime createdAt;
    private Integer fileSize;
    private Boolean shareStatus;
    private Boolean favorite;
    private Integer favoriteSort;
    private LocalDateTime favoritedAt;
    private Integer views;
    private String platform;
    private String originUrl;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.shareStatus = false;
        this.favorite = false;
        this.favoriteSort = 0;
        this.views = 0;
    }
}

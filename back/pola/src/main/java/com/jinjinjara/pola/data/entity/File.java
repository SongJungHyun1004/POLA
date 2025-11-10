package com.jinjinjara.pola.data.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 255)
    private String src; // S3 링크

    @Column(nullable = false, length = 255)
    private String type; // 파일 MIME 타입

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 255)
    private String context; // LLM 컨텍스트 (기본값 Llava)

    @Column(name = "ocr_text", columnDefinition = "TEXT")
    private String ocrText; // OCR 결과

    @Column(name = "vector_id")
    private Long vectorId; // 벡터 ID

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "share_status", nullable = false)
    private Boolean shareStatus;

    @Column(nullable = false)
    private Boolean favorite;

    @Column(name = "favorite_sort", nullable = false)
    private Integer favoriteSort;

    @Column(name = "favorited_at", nullable = false)
    private LocalDateTime favoritedAt;

    @Column(nullable = false)
    private Integer views;

    @Column(length = 255)
    private String platform; // 업로드 플랫폼 (웹, 앱 등)

    @Column(name = "origin_url", length = 255)
    private String originUrl; // 원본 URL (선택)

    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt; //  마지막 열람 시각

    /* --- 콜백 영역 --- */
    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (context == null) context = "Llava";
        if (shareStatus == null) shareStatus = false;
        if (favorite == null) favorite = false;
        if (favoriteSort == null) favoriteSort = 0;
        if (favoritedAt == null) favoritedAt = now;
        if (views == null) views = 0;
    }

    @PreUpdate
    public void preUpdate() {
    }
}

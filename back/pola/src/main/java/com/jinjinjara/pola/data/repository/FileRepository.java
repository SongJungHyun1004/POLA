package com.jinjinjara.pola.data.repository;

import com.jinjinjara.pola.data.entity.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {

    /* ----------------------- [즐겨찾기 관련] ----------------------- */

    // 즐겨찾기 전체 조회 (정렬용)
    List<File> findAllByUserIdAndFavoriteTrueOrderByFavoriteSortAscFavoritedAtDesc(Long userId);

    // 즐겨찾기 페이지 조회 (페이징)
    Page<File> findAllByUserIdAndFavoriteTrueOrderByFavoriteSortAscFavoritedAtDesc(Long userId, Pageable pageable);
    Page<File> findAllByUserIdAndFavoriteTrue(Long userId, Pageable pageable);

    // 즐겨찾기 정렬 순서 밀기 (+1)
    @Modifying
    @Query("""
        UPDATE File f 
           SET f.favoriteSort = f.favoriteSort + 1
         WHERE f.userId = :userId 
           AND f.favorite = true
           AND f.favoriteSort >= :start 
           AND f.favoriteSort < :end
        """)
    void incrementSortRange(@Param("userId") Long userId,
                            @Param("start") int start,
                            @Param("end") int end);

    // 즐겨찾기 정렬 순서 당기기 (-1)
    @Modifying
    @Query("""
        UPDATE File f 
           SET f.favoriteSort = f.favoriteSort - 1
         WHERE f.userId = :userId 
           AND f.favorite = true
           AND f.favoriteSort > :start 
           AND f.favoriteSort <= :end
        """)
    void decrementSortRange(@Param("userId") Long userId,
                            @Param("start") int start,
                            @Param("end") int end);

    @Query("""
    SELECT f FROM File f
    WHERE f.userId = :userId
      AND (f.lastViewedAt IS NULL OR f.lastViewedAt < :sevenDaysAgo)
    ORDER BY f.views ASC, f.lastViewedAt ASC NULLS FIRST, f.createdAt ASC
    """)
    List<File> findRemindFiles(@Param("userId") Long userId,
                               @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo,
                               Pageable pageable);

    List<File> findTop5ByUserIdAndCategoryIdOrderByCreatedAtDesc(Long userId, Long categoryId);

    List<File> findTop3ByUserIdAndFavoriteTrueOrderByCreatedAtDesc(Long userId);

    List<File> findTop10ByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
        SELECT f FROM File f
        WHERE f.userId = :userId
          AND (f.lastViewedAt IS NULL OR f.lastViewedAt < :sevenDaysAgo)
        ORDER BY f.views ASC, f.createdAt DESC
        """)
    List<File> findRemindPreview(@Param("userId") Long userId,
                                 @Param("sevenDaysAgo") LocalDateTime sevenDaysAgo,
                                 Pageable pageable);

    Optional<File> findByIdAndUserId(Long id, Long userId);

    // 유저 전체 파일 (페이징)
    Page<File> findAllByUserId(Long userId, Pageable pageable);

    // 카테고리별 파일 (페이징)
    Page<File> findAllByUserIdAndCategoryId(Long userId, Long categoryId, Pageable pageable);

    // 공유 파일 (필요시)
    Page<File> findAllByUserIdAndShareStatusTrue(Long userId, Pageable pageable);

    // 특정 플랫폼별 파일 (e.g. "web", "app")
    Page<File> findAllByUserIdAndPlatform(Long userId, String platform, Pageable pageable);
}

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

    List<File> findAllByUserIdAndFavoriteTrueOrderByFavoriteSortAscFavoritedAtDesc(Long userId);

    @Query("SELECT f FROM File f " +
            "WHERE f.userId = :userId " +
            "ORDER BY f.views ASC, f.createdAt DESC")
    List<File> findLeastViewedFiles(
            @Param("userId") Long userId,
            Pageable pageable
    );

    Page<File> findAllByUserIdAndFavoriteTrueOrderByFavoriteSortAscFavoritedAtDesc(Long userId, Pageable pageable);

    Page<File> findAllByUserIdAndFavoriteTrue(Long userId, Pageable pageable);

    Optional<File> findByShareToken(String shareToken);

    @Query(value = """
            SELECT *
            FROM (
                SELECT f.*,
                       ROW_NUMBER() OVER (PARTITION BY f.category_id ORDER BY f.created_at DESC) AS rn
                FROM files f
                WHERE f.user_id = :userId
            ) sub
            WHERE sub.rn <= 5
            """, nativeQuery = true)
    List<File> findTop5FilesPerCategory(@Param("userId") Long userId);

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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE File f SET f.shareExpiredAt = :expiredAt WHERE f.id = :id")
    void updateShareExpiredAt(@Param("id") Long id, @Param("expiredAt") LocalDateTime expiredAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE File f SET f.shareToken = :token, f.shareExpiredAt = :expiredAt WHERE f.id = :id")
    void updateShareTokenAndExpiredAt(
            @Param("id") Long id,
            @Param("token") String token,
            @Param("expiredAt") LocalDateTime expiredAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE File f SET
            f.shareStatus = true,
            f.shareToken = :token,
            f.shareExpiredAt = :expiredAt
        WHERE f.id = :id
        """)
    void updateShareInfo(
            @Param("id") Long id,
            @Param("token") String token,
            @Param("expiredAt") LocalDateTime expiredAt);

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

    @Query("SELECT f FROM FileTag ft JOIN ft.file f " +
            "WHERE f.categoryId = :categoryId AND ft.tag.id = :tagId " +
            "ORDER BY f.createdAt DESC")
    Optional<File> findTopByCategoryIdAndTagIdOrderByCreatedAtDesc(
            @Param("categoryId") Long categoryId,
            @Param("tagId") Long tagId
    );

    List<File> findAllByCategoryId(Long categoryId);

    @Query("""
            SELECT f
            FROM File f
            JOIN FileTag ft ON f.id = ft.file.id
            WHERE f.userId = :userId
              AND ft.tag.id = :tagId
            ORDER BY f.createdAt DESC
            """)
    Page<File> findAllByUserIdAndTagId(@Param("userId") Long userId,
                                       @Param("tagId") Long tagId,
                                       Pageable pageable);

    Page<File> findAllByUserId(Long userId, Pageable pageable);

    Page<File> findAllByUserIdAndCategoryId(Long userId, Long categoryId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE File f SET
                f.categoryId = :categoryId,
                f.context    = :context,
                f.ocrText    = :ocrText,
                f.vectorId   = :vectorId
            WHERE f.id = :fileId
              AND f.userId = :userId
            """)
    int updatePostProcessing(
            @Param("fileId") Long fileId,
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("context") String context,
            @Param("ocrText") String ocrText,
            @Param("vectorId") Long vectorId
    );
}

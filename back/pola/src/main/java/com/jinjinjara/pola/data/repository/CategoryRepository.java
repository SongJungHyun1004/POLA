package com.jinjinjara.pola.data.repository;

import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUser(Users user);

    Optional<Category> findByUserAndCategoryName(Users user, String categoryName);

    boolean existsByUserAndCategoryName(Users user, String categoryName);

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


    @Query("SELECT c FROM Category c " +
            "WHERE c.user = :user " +
            "ORDER BY c.fileCount DESC, " +
            "CASE WHEN c.categoryName = '미분류' THEN 1 ELSE 0 END ASC, " +
            "c.categoryName ASC")
    List<Category> findAllSortedByUser(@Param("user") Users user);


    @Query("SELECT c FROM Category c " +
            "WHERE c.user.id = :userId " +
            "ORDER BY c.fileCount DESC, " +
            "CASE WHEN c.categoryName = '미분류' THEN 1 ELSE 0 END ASC, " +
            "c.categoryName ASC")
    List<Category> findAllSorted(Long userId);


    @Query("SELECT c.id FROM Category c WHERE c.user.id = :userId AND c.categoryName = :categoryName")
    Optional<Long> findIdByUserIdAndCategoryName(@Param("userId") Long userId, @Param("categoryName") String categoryName);

    Optional<Category> findByUserIdAndCategoryName(Long userId, String categoryName);

    @Query("SELECT c.user.id FROM Category c WHERE c.id = :categoryId")
    Long findOwnerUserIdByCategoryId(@Param("categoryId") Long categoryId);
}

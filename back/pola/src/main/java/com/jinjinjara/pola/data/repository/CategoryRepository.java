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

    @Query("SELECT c FROM Category c WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    List<Category> findAllByUserId(@Param("userId") Long userId);

    Optional<Category> findByIdAndUserId(Long id, Long userId);

    @Query("""
                SELECT c FROM Category c
                LEFT JOIN File f ON f.categoryId = c.id
                WHERE c.user.id = :userId
                GROUP BY c.id
                ORDER BY 
                    COUNT(f.id) DESC,
                    CASE WHEN c.categoryName = '미분류' THEN 1 ELSE 0 END ASC
            """)
    List<Category> findAllByUserIdOrderByFileCountDesc(Long userId);


    @Query("SELECT c.id FROM Category c WHERE c.user.id = :userId AND c.categoryName = :categoryName")
    Optional<Long> findIdByUserIdAndCategoryName(@Param("userId") Long userId, @Param("categoryName") String categoryName);

    Optional<Category> findByUserIdAndCategoryName(Long userId, String categoryName);

    @Query("SELECT c.user.id FROM Category c WHERE c.id = :categoryId")
    Long findOwnerUserIdByCategoryId(@Param("categoryId") Long categoryId);
}

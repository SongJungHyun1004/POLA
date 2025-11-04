package com.jinjinjara.pola.data.repository;

import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByUser(Users user);
    Optional<Category> findByUserAndCategoryName(Users user, String categoryName);
    boolean existsByUserAndCategoryName(Users user, String categoryName);
}

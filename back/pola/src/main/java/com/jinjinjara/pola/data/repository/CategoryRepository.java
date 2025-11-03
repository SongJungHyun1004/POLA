package com.jinjinjara.pola.data.repository;

import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {


    Optional<Category> findByUserAndCategoryName(Users user, String categoryName);

    Optional<Category> findByUserIdAndCategoryName(Users user, String categoryName);
}

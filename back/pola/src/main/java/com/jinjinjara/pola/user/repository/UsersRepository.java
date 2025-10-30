package com.jinjinjara.pola.user.repository;

import com.jinjinjara.pola.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Users> findByDisplayName(String displayName);

    Optional<Users> findByGoogleSub(String googleSub);
}
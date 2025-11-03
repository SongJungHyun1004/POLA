package com.jinjinjara.pola.auth.repository;


import com.jinjinjara.pola.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {

    // 이메일로 사용자 찾기
    Optional<Users> findByEmail(String email);

    // 구글 서브 ID로 사용자 찾기
    Optional<Users> findByGoogleSub(String googleSub);

    // 특정 역할(role) 가진 사용자 존재 여부
    boolean existsByRole(String role);
}

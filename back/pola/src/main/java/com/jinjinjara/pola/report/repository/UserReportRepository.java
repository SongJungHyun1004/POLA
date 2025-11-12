package com.jinjinjara.pola.report.repository;

import com.jinjinjara.pola.report.entity.UserReport;
import com.jinjinjara.pola.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 리포트 Repository
 */
@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {

    /**
     * 특정 사용자의 모든 리포트 조회 (최신순)
     */
    List<UserReport> findByUserOrderByCreatedAtDesc(Users user);

    /**
     * 특정 사용자의 특정 주차 리포트 조회
     */
    Optional<UserReport> findByUserAndReportWeek(Users user, String reportWeek);

    /**
     * 특정 주차의 모든 리포트 조회
     */
    List<UserReport> findByReportWeek(String reportWeek);

    /**
     * 특정 주차에 이미 리포트가 존재하는지 확인
     */
    boolean existsByUserAndReportWeek(Users user, String reportWeek);
}

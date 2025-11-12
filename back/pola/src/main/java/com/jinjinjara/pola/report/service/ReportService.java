package com.jinjinjara.pola.report.service;

import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.repository.CategoryRepository;
import com.jinjinjara.pola.data.repository.FileRepository;
import com.jinjinjara.pola.report.entity.ReportType;
import com.jinjinjara.pola.report.entity.UserReport;
import com.jinjinjara.pola.report.repository.UserReportRepository;
import com.jinjinjara.pola.user.entity.Users;
import com.jinjinjara.pola.user.repository.UsersRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 사용자 수집 페르소나 리포트 생성 서비스
 *
 * 역할:
 * 1. 사용자별 콘텐츠 수집 패턴 분석
 * 2. 페르소나 타입 결정 (Rule Engine)
 * 3. UserReport 엔티티 생성 및 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final FileRepository fileRepository;
    private final CategoryRepository categoryRepository;
    private final UserReportRepository userReportRepository;
    private final UsersRepository usersRepository;
    private final EntityManager entityManager;

    // 페르소나 판정 임계값
    private static final double TAG_ONE_WELL_THRESHOLD = 0.70;      // 70%
    private static final double SCREENSHOT_MASTER_THRESHOLD = 0.80; // 80%
    private static final int OCTOPUS_COLLECTOR_MIN_CATEGORIES = 5;  // 5개 이상
    private static final double TRIPITAKA_MASTER_THRESHOLD = 0.80;  // 80%
    private static final double NIGHT_OWL_THRESHOLD = 0.60;         // 60%
    private static final double MIRACLE_MORNING_THRESHOLD = 0.60;   // 60%

    /**
     * 주간 리포트 생성 (모든 사용자 대상)
     *
     * @param startDate 분석 시작일
     * @param endDate 분석 종료일
     * @param reportLabel 리포트 주차 레이블 (예: "2025-W45" 또는 "Demo-2025-01-15")
     */
    @Transactional
    public void generateReports(LocalDate startDate, LocalDate endDate, String reportLabel) {
        log.info("리포트 생성 시작: {} ~ {} (레이블: {})", startDate, endDate, reportLabel);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 모든 사용자 조회
        List<Users> allUsers = usersRepository.findAll();
        log.info("총 {} 명의 사용자 대상 리포트 생성 시작", allUsers.size());

        int successCount = 0;
        int skipCount = 0;
        int noTypeCount = 0;

        for (Users user : allUsers) {
            try {
                // 이미 해당 주차 리포트가 존재하면 스킵
                if (userReportRepository.existsByUserAndReportWeek(user, reportLabel)) {
                    log.debug("사용자 {} ({}): 이미 리포트 존재, 스킵", user.getId(), user.getEmail());
                    skipCount++;
                    continue;
                }

                // 사용자별 리포트 생성
                UserReport report = generateReportForUser(user, startDateTime, endDateTime, reportLabel);

                if (report.getReportType() == ReportType.NO_TYPE) {
                    noTypeCount++;
                }

                userReportRepository.save(report);
                successCount++;

                log.info("사용자 {} ({}): 리포트 생성 완료 - 타입: {}, 점수: {}",
                        user.getId(), user.getEmail(), report.getReportType(), report.getScore());

            } catch (Exception e) {
                log.error("사용자 {} 리포트 생성 실패: {}", user.getId(), e.getMessage(), e);
            }
        }

        log.info("리포트 생성 완료: 성공 {}, 스킵 {}, 타입없음 {}, 전체 {}",
                successCount, skipCount, noTypeCount, allUsers.size());
    }

    /**
     * 특정 사용자의 리포트 생성
     */
    private UserReport generateReportForUser(
            Users user,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String reportLabel
    ) {
        // 1. 데이터 집계
        UserStats stats = aggregateUserStats(user.getId(), startDateTime, endDateTime);

        // 2. 페르소나 타입 결정
        ReportTypeScore bestScore = determineReportType(stats);

        // 3. UserReport 엔티티 생성
        return UserReport.fromReportType(
                user,
                bestScore.getReportType(),
                reportLabel,
                startDateTime,
                endDateTime,
                bestScore.getScore()
        );
    }

    /**
     * 사용자 통계 집계
     */
    private UserStats aggregateUserStats(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        // Native Query로 효율적으로 집계
        String sql = """
            SELECT
                COUNT(*) as total_count,
                COUNT(CASE WHEN type LIKE 'image/%' THEN 1 END) as image_count,
                COUNT(CASE WHEN type LIKE 'text/%' THEN 1 END) as text_count,
                COUNT(CASE WHEN EXTRACT(HOUR FROM created_at) BETWEEN 22 AND 23
                            OR EXTRACT(HOUR FROM created_at) BETWEEN 0 AND 3 THEN 1 END) as night_count,
                COUNT(CASE WHEN EXTRACT(HOUR FROM created_at) BETWEEN 6 AND 10 THEN 1 END) as morning_count
            FROM files
            WHERE user_id = :userId
              AND created_at BETWEEN :startDateTime AND :endDateTime
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        query.setParameter("startDateTime", startDateTime);
        query.setParameter("endDateTime", endDateTime);

        Object[] result = (Object[]) query.getSingleResult();

        long totalCount = ((Number) result[0]).longValue();
        long imageCount = ((Number) result[1]).longValue();
        long textCount = ((Number) result[2]).longValue();
        long nightCount = ((Number) result[3]).longValue();
        long morningCount = ((Number) result[4]).longValue();

        // 카테고리별 분포 계산
        Map<Long, Long> categoryDistribution = getCategoryDistribution(userId, startDateTime, endDateTime);

        return UserStats.builder()
                .totalCount(totalCount)
                .imageCount(imageCount)
                .textCount(textCount)
                .nightCount(nightCount)
                .morningCount(morningCount)
                .categoryDistribution(categoryDistribution)
                .build();
    }

    /**
     * 카테고리별 콘텐츠 분포 조회
     */
    private Map<Long, Long> getCategoryDistribution(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        String sql = """
            SELECT category_id, COUNT(*) as count
            FROM files
            WHERE user_id = :userId
              AND created_at BETWEEN :startDateTime AND :endDateTime
            GROUP BY category_id
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("userId", userId);
        query.setParameter("startDateTime", startDateTime);
        query.setParameter("endDateTime", endDateTime);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();

        return results.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));
    }

    /**
     * 페르소나 타입 결정 (Rule Engine)
     * 모든 타입의 점수를 계산하고 가장 높은 점수의 타입을 반환
     */
    private ReportTypeScore determineReportType(UserStats stats) {
        // 콘텐츠가 너무 적으면 NO_TYPE
        if (stats.getTotalCount() < 5) {
            log.debug("콘텐츠 수 부족 ({}개)", stats.getTotalCount());
            return new ReportTypeScore(ReportType.NO_TYPE, 0.0);
        }

        List<ReportTypeScore> scores = new ArrayList<>();

        // 1. 태그한우물 (특정 카테고리 집중도)
        scores.add(calculateTagOneWellScore(stats));

        // 2. 스크린샷 장인 (이미지 비율)
        scores.add(calculateScreenshotMasterScore(stats));

        // 3. 문어발 수집가 (카테고리 다양성)
        scores.add(calculateOctopusCollectorScore(stats));

        // 4. 팔만대장경 장인 (텍스트 비율)
        scores.add(calculateTripitakaMasterScore(stats));

        // 5. 밤도깨비 (야간 활동 비율)
        scores.add(calculateNightOwlScore(stats));

        // 6. 미라클 모닝곰 (아침 활동 비율)
        scores.add(calculateMiracleMorningScore(stats));

        // 가장 높은 점수의 타입 선택
        ReportTypeScore bestScore = scores.stream()
                .max(Comparator.comparing(ReportTypeScore::getScore))
                .orElse(new ReportTypeScore(ReportType.NO_TYPE, 0.0));

        // 점수가 0이면 NO_TYPE
        if (bestScore.getScore() == 0.0) {
            return new ReportTypeScore(ReportType.NO_TYPE, 0.0);
        }

        log.debug("최고 점수: {} ({}점)", bestScore.getReportType(), bestScore.getScore());
        return bestScore;
    }

    // ==================== 각 타입별 점수 계산 메서드 ====================

    private ReportTypeScore calculateTagOneWellScore(UserStats stats) {
        if (stats.getCategoryDistribution().isEmpty()) {
            return new ReportTypeScore(ReportType.TAG_ONE_WELL, 0.0);
        }

        // 상위 카테고리의 비율 계산
        long maxCategoryCount = stats.getCategoryDistribution().values().stream()
                .max(Long::compareTo)
                .orElse(0L);

        double ratio = (double) maxCategoryCount / stats.getTotalCount();

        if (ratio >= TAG_ONE_WELL_THRESHOLD) {
            return new ReportTypeScore(ReportType.TAG_ONE_WELL, ratio);
        }

        return new ReportTypeScore(ReportType.TAG_ONE_WELL, 0.0);
    }

    private ReportTypeScore calculateScreenshotMasterScore(UserStats stats) {
        double imageRatio = (double) stats.getImageCount() / stats.getTotalCount();

        if (imageRatio >= SCREENSHOT_MASTER_THRESHOLD) {
            return new ReportTypeScore(ReportType.SCREENSHOT_MASTER, imageRatio);
        }

        return new ReportTypeScore(ReportType.SCREENSHOT_MASTER, 0.0);
    }

    private ReportTypeScore calculateOctopusCollectorScore(UserStats stats) {
        int categoryCount = stats.getCategoryDistribution().size();

        if (categoryCount >= OCTOPUS_COLLECTOR_MIN_CATEGORIES) {
            // 카테고리가 많을수록 높은 점수
            double score = Math.min(1.0, (double) categoryCount / 10.0);
            return new ReportTypeScore(ReportType.OCTOPUS_COLLECTOR, score);
        }

        return new ReportTypeScore(ReportType.OCTOPUS_COLLECTOR, 0.0);
    }

    private ReportTypeScore calculateTripitakaMasterScore(UserStats stats) {
        double textRatio = (double) stats.getTextCount() / stats.getTotalCount();

        if (textRatio >= TRIPITAKA_MASTER_THRESHOLD) {
            return new ReportTypeScore(ReportType.TRIPITAKA_MASTER, textRatio);
        }

        return new ReportTypeScore(ReportType.TRIPITAKA_MASTER, 0.0);
    }

    private ReportTypeScore calculateNightOwlScore(UserStats stats) {
        double nightRatio = (double) stats.getNightCount() / stats.getTotalCount();

        if (nightRatio >= NIGHT_OWL_THRESHOLD) {
            return new ReportTypeScore(ReportType.NIGHT_OWL, nightRatio);
        }

        return new ReportTypeScore(ReportType.NIGHT_OWL, 0.0);
    }

    private ReportTypeScore calculateMiracleMorningScore(UserStats stats) {
        double morningRatio = (double) stats.getMorningCount() / stats.getTotalCount();

        if (morningRatio >= MIRACLE_MORNING_THRESHOLD) {
            return new ReportTypeScore(ReportType.MIRACLE_MORNING_BEAR, morningRatio);
        }

        return new ReportTypeScore(ReportType.MIRACLE_MORNING_BEAR, 0.0);
    }

    // ==================== 내부 클래스 ====================

    /**
     * 사용자 통계 DTO
     */
    @lombok.Builder
    @lombok.Getter
    private static class UserStats {
        private final long totalCount;
        private final long imageCount;
        private final long textCount;
        private final long nightCount;
        private final long morningCount;
        private final Map<Long, Long> categoryDistribution;
    }

    /**
     * 리포트 타입과 점수 DTO
     */
    @lombok.AllArgsConstructor
    @lombok.Getter
    private static class ReportTypeScore {
        private final ReportType reportType;
        private final double score;
    }

    // ==================== 테스트용 메서드 ====================

    /**
     * 이번주 리포트 삭제 (테스트용)
     *
     * @param user 삭제할 사용자
     */
    @Transactional
    public void deleteCurrentWeekReport(Users user) {
        // 이번주의 월요일과 일요일 계산
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // 이번주 레이블 생성: "Demo-YYYY-MM-DD" 형식으로 오늘 날짜 사용
        String currentWeekLabel = "Demo-" + today.toString();

        log.info("🗑️ 이번주 리포트 삭제 시도: 사용자 ID {}, 레이블: {}", user.getId(), currentWeekLabel);

        // 해당 주차 리포트 조회
        Optional<UserReport> reportOpt = userReportRepository.findByUserAndReportWeek(user, currentWeekLabel);

        if (reportOpt.isEmpty()) {
            log.warn("⚠️ 삭제할 리포트가 없습니다: 사용자 ID {}, 레이블: {}", user.getId(), currentWeekLabel);
            throw new CustomException(ErrorCode.REPORT_NOT_FOUND, "이번주 리포트가 존재하지 않습니다.");
        }

        userReportRepository.delete(reportOpt.get());
        log.info("✅ 이번주 리포트 삭제 완료: 사용자 ID {}, 타입: {}", user.getId(), reportOpt.get().getReportType());
    }

    /**
     * 특정 타입으로 리포트 강제 생성 (테스트용)
     * 실제 데이터 분석 없이 지정된 타입으로 리포트를 생성합니다.
     *
     * @param user 리포트를 생성할 사용자
     * @param reportType 강제로 지정할 리포트 타입
     */
    @Transactional
    public void generateReportWithType(Users user, ReportType reportType) {
        // 이번주의 월요일과 일요일 계산
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        LocalDateTime startDateTime = monday.atStartOfDay();
        LocalDateTime endDateTime = sunday.atTime(LocalTime.MAX);

        // 이번주 레이블 생성: "Demo-YYYY-MM-DD" 형식
        String currentWeekLabel = "Demo-" + today.toString();

        log.info("🎯 특정 타입으로 리포트 강제 생성: 사용자 ID {}, 타입: {}, 레이블: {}",
                user.getId(), reportType, currentWeekLabel);

        // 이미 해당 주차 리포트가 존재하면 예외
        if (userReportRepository.existsByUserAndReportWeek(user, currentWeekLabel)) {
            log.warn("⚠️ 이미 리포트가 존재합니다: 사용자 ID {}, 레이블: {}", user.getId(), currentWeekLabel);
            throw new CustomException(ErrorCode.REPORT_ALREADY_EXISTS,
                    "이번주 리포트가 이미 존재합니다. 먼저 삭제 후 다시 시도하세요.");
        }

        // 강제로 지정된 타입으로 UserReport 생성
        UserReport report = UserReport.fromReportType(
                user,
                reportType,
                currentWeekLabel,
                startDateTime,
                endDateTime,
                1.0  // 테스트용이므로 점수는 1.0으로 고정
        );

        userReportRepository.save(report);
        log.info("✅ 특정 타입으로 리포트 생성 완료: 사용자 ID {}, 타입: {}", user.getId(), reportType);
    }
}

package com.jinjinjara.pola.report.scheduler;

import com.jinjinjara.pola.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;

/**
 * 주간 리포트 자동 생성 스케줄러
 *
 * 역할:
 * - 매주 일요일 오전 3시에 "지난주" (월~일) 리포트를 자동 생성
 * - 리포트 주차 레이블: "2025-W45" 형식
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportScheduler {

    private final ReportService reportService;

    /**
     * 주간 리포트 자동 생성
     * Cron: 매주 일요일 오전 3시 실행
     *
     * 분석 기간: 지난주 월요일 00:00 ~ 일요일 23:59
     * 리포트 레이블: "YYYY-WNN" (예: "2025-W45")
     */
    @Scheduled(cron = "0 0 3 * * SUN", zone = "Asia/Seoul")
    public void generateWeeklyReports() {
        log.info("⏰ [스케줄러] 주간 리포트 생성 시작");

        try {
            // 지난주 계산
            LocalDate today = LocalDate.now();
            LocalDate lastSunday = today.minusDays(1); // 오늘이 일요일이므로 -1일
            LocalDate lastMonday = lastSunday.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            // ISO 주차 계산 (예: 2025-W45)
            int year = lastMonday.getYear();
            int weekNumber = lastMonday.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
            String reportLabel = String.format("%d-W%02d", year, weekNumber);

            log.info("지난주 기간: {} ~ {}", lastMonday, lastSunday);
            log.info("리포트 레이블: {}", reportLabel);

            // 리포트 생성 실행
            reportService.generateReports(lastMonday, lastSunday, reportLabel);

            log.info("[스케줄러] 주간 리포트 생성 완료");

        } catch (Exception e) {
            log.error("[스케줄러] 주간 리포트 생성 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 테스트용 스케줄러 (주석 처리)
     * 개발 환경에서 테스트 시에만 활성화
     */
    // @Scheduled(cron = "0 */5 * * * *", zone = "Asia/Seoul") // 5분마다 실행
    public void generateTestReports() {
        log.info("[테스트 스케줄러] 리포트 생성 시작");

        try {
            LocalDate today = LocalDate.now();
            LocalDate weekAgo = today.minusDays(7);
            String reportLabel = "Test-" + today.toString();

            reportService.generateReports(weekAgo, today, reportLabel);

            log.info("[테스트 스케줄러] 리포트 생성 완료");

        } catch (Exception e) {
            log.error("[테스트 스케줄러] 리포트 생성 실패: {}", e.getMessage(), e);
        }
    }
}

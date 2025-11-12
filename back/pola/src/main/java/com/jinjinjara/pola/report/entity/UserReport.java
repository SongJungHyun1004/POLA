package com.jinjinjara.pola.report.entity;

import com.jinjinjara.pola.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자 주간 수집 페르소나 리포트 엔티티
 * 주간 또는 수동으로 생성된 리포트 결과를 저장
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "user_reports",
        indexes = {
                @Index(name = "idx_user_report_week", columnList = "user_id, report_week"),
                @Index(name = "idx_report_week", columnList = "report_week")
        }
)
public class UserReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false, length = 50)
    private ReportType reportType;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    /**
     * 리포트 주차 레이블
     * 예: "2025-W45" (주간 자동 생성)
     * 예: "Demo-2025-01-15" (테스트 할때 쓰는  수동 생성)
     */
    @Column(name = "report_week", nullable = false, length = 50)
    private String reportWeek;

    /**
     * 리포트 생성 시간
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 분석 대상 기간 시작일
     */
    @Column(name = "analysis_start_date", nullable = false)
    private LocalDateTime analysisStartDate;

    /**
     * 분석 대상 기간 종료일
     */
    @Column(name = "analysis_end_date", nullable = false)
    private LocalDateTime analysisEndDate;

    /**
     * 리포트 타입별 점수 (선택적, 디버깅용)
     */
    @Column(name = "score")
    private Double score;

    /**
     * 팩토리 메서드: ReportType Enum으로부터 UserReport 생성
     */
    public static UserReport fromReportType(
            Users user,
            ReportType reportType,
            String reportWeek,
            LocalDateTime analysisStartDate,
            LocalDateTime analysisEndDate,
            Double score
    ) {
        return UserReport.builder()
                .user(user)
                .reportType(reportType)
                .title(reportType.getTitle())
                .description(reportType.getDescription())
                .imageUrl(reportType.getS3ImageUrl())
                .reportWeek(reportWeek)
                .analysisStartDate(analysisStartDate)
                .analysisEndDate(analysisEndDate)
                .score(score)
                .build();
    }
}

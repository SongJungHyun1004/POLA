package com.jinjinjara.pola.report.dto.response;

import com.jinjinjara.pola.report.entity.ReportType;
import com.jinjinjara.pola.report.entity.UserReport;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 리포트 응답 DTO
 */
@Getter
@Builder
public class UserReportResponse {

    private Long id;
    private String reportType;
    private String title;
    private String description;
    private String imageUrl;
    private String reportWeek;
    private LocalDateTime createdAt;
    private LocalDateTime analysisStartDate;
    private LocalDateTime analysisEndDate;
    private Double score;

    /**
     * Entity → DTO 변환
     */
    public static UserReportResponse from(UserReport report) {
        return UserReportResponse.builder()
                .id(report.getId())
                .reportType(report.getReportType().name())
                .title(report.getTitle())
                .description(report.getDescription())
                .imageUrl(report.getImageUrl())
                .reportWeek(report.getReportWeek())
                .createdAt(report.getCreatedAt())
                .analysisStartDate(report.getAnalysisStartDate())
                .analysisEndDate(report.getAnalysisEndDate())
                .score(report.getScore())
                .build();
    }
}

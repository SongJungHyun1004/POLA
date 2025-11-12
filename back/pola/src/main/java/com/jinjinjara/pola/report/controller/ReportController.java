package com.jinjinjara.pola.report.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.report.dto.response.UserReportResponse;
import com.jinjinjara.pola.report.entity.UserReport;
import com.jinjinjara.pola.report.repository.UserReportRepository;
import com.jinjinjara.pola.user.entity.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 리포트 조회 컨트롤러
 *
 * 역할:
 * - 사용자가 자신의 리포트를 조회하는 API 제공
 */
@Tag(name = "Report API", description = "사용자 리포트 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/v1/users/me/reports")
@RequiredArgsConstructor
public class ReportController {

    private final UserReportRepository userReportRepository;

    @Operation(
            summary = "내 리포트 전체 조회",
            description = """
                    현재 로그인한 사용자의 모든 리포트를 최신순으로 조회합니다.

                    **응답 예시:**
                    ```json
                    {
                      "status": "SUCCESS",
                      "message": "리포트 목록 조회 완료",
                      "data": [
                        {
                          "id": 1,
                          "reportType": "SCREENSHOT_MASTER",
                          "title": "스크린샷 장인",
                          "description": "백 마디 말보다 한 장의 캡쳐가 중요하죠...",
                          "imageUrl": "https://pola-storage-bucket.s3.../screenshot_master.png",
                          "reportWeek": "2025-W03",
                          "createdAt": "2025-01-21T03:00:00",
                          "analysisStartDate": "2025-01-13T00:00:00",
                          "analysisEndDate": "2025-01-19T23:59:59",
                          "score": 0.85
                        }
                      ]
                    }
                    ```
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping
    public ApiResponse<List<UserReportResponse>> getMyReports(
            @Parameter(hidden = true) @AuthenticationPrincipal Users user) {

        log.info("사용자 {} 리포트 전체 조회", user.getId());

        List<UserReport> reports = userReportRepository.findByUserOrderByCreatedAtDesc(user);

        List<UserReportResponse> responses = reports.stream()
                .map(UserReportResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.ok(responses, "리포트 목록 조회 완료");
    }

    @Operation(
            summary = "특정 주차 리포트 조회",
            description = """
                    특정 주차의 리포트를 조회합니다.

                    **경로 매개변수:**
                    - `reportWeek`: 리포트 주차 레이블 (예: "2025-W03" 또는 "Demo-2025-01-21")

                    **응답 예시:**
                    ```json
                    {
                      "status": "SUCCESS",
                      "message": "리포트 조회 완료",
                      "data": {
                        "id": 1,
                        "reportType": "TAG_ONE_WELL",
                        "title": "태그한우물",
                        "description": "당신은 관심 분야가 확고한 사람이에요...",
                        "imageUrl": "https://pola-storage-bucket.s3.../tag_one_well.png",
                        "reportWeek": "2025-W03",
                        "createdAt": "2025-01-21T03:00:00",
                        "analysisStartDate": "2025-01-13T00:00:00",
                        "analysisEndDate": "2025-01-19T23:59:59",
                        "score": 0.72
                      }
                    }
                    ```

                    **에러 응답 (404 Not Found):**
                    ```json
                    {
                      "status": "FAIL",
                      "code": "REPORT-001",
                      "message": "리포트를 찾을 수 없습니다."
                    }
                    ```
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/{reportWeek}")
    public ApiResponse<UserReportResponse> getReportByWeek(
            @Parameter(description = "리포트 주차 레이블", example = "2025-W03")
            @PathVariable String reportWeek,
            @Parameter(hidden = true) @AuthenticationPrincipal Users user) {

        log.info("사용자 {} 리포트 조회: {}", user.getId(), reportWeek);

        UserReport report = userReportRepository.findByUserAndReportWeek(user, reportWeek)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        UserReportResponse response = UserReportResponse.from(report);

        return ApiResponse.ok(response, "리포트 조회 완료");
    }

    @Operation(
            summary = "최신 리포트 조회",
            description = """
                    가장 최근에 생성된 리포트를 조회합니다.

                    **응답 예시:**
                    ```json
                    {
                      "status": "SUCCESS",
                      "message": "최신 리포트 조회 완료",
                      "data": {
                        "id": 5,
                        "reportType": "NIGHT_OWL",
                        "title": "밤도깨비",
                        "description": "밤이 되면 활동하는 야행성 수집가...",
                        "imageUrl": "https://pola-storage-bucket.s3.../night_owl.png",
                        "reportWeek": "2025-W03",
                        "createdAt": "2025-01-21T03:00:00",
                        "analysisStartDate": "2025-01-13T00:00:00",
                        "analysisEndDate": "2025-01-19T23:59:59",
                        "score": 0.68
                      }
                    }
                    ```

                    **에러 응답 (404 Not Found):**
                    ```json
                    {
                      "status": "FAIL",
                      "code": "REPORT-001",
                      "message": "리포트를 찾을 수 없습니다."
                    }
                    ```
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/latest")
    public ApiResponse<UserReportResponse> getLatestReport(
            @Parameter(hidden = true) @AuthenticationPrincipal Users user) {

        log.info("사용자 {} 최신 리포트 조회", user.getId());

        List<UserReport> reports = userReportRepository.findByUserOrderByCreatedAtDesc(user);

        if (reports.isEmpty()) {
            throw new CustomException(ErrorCode.REPORT_NOT_FOUND);
        }

        UserReport latestReport = reports.get(0);
        UserReportResponse response = UserReportResponse.from(latestReport);

        return ApiResponse.ok(response, "최신 리포트 조회 완료");
    }
}

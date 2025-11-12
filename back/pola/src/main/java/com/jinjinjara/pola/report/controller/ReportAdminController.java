package com.jinjinjara.pola.report.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.report.dto.request.GenerateWithTypeRequest;
import com.jinjinjara.pola.report.service.ReportService;
import com.jinjinjara.pola.user.entity.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 리포트 관리자 컨트롤러
 *
 * 역할:
 * - 수동으로 리포트를 즉시 생성하는 API 제공
 * - 시연 및 테스트 목적으로 사용
 * - ADMIN 권한 필요
 */
@Tag(name = "Report Admin API", description = "리포트 관리자 API (수동 생성)")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/reports")
@RequiredArgsConstructor
public class ReportAdminController {

    private final ReportService reportService;

    @Operation(
            summary = "리포트 수동 생성 (관리자 전용)",
            description = """
                    최근 7일간의 데이터를 기준으로 모든 사용자의 리포트를 즉시 생성합니다.

                    **권한:** ADMIN만 접근 가능

                    **분석 기간:** 오늘 기준 D-6 ~ 오늘 (최근 7일)

                    **리포트 레이블:** "Demo-YYYY-MM-DD" 형식

                    **사용 목적:**
                    - 시연 및 테스트
                    - 주간 스케줄러 대기 없이 즉시 리포트 생성

                    **주의사항:**
                    - 동일한 날짜에 여러 번 실행하면 중복 생성되지 않음 (같은 레이블은 스킵)
                    - 프로덕션 환경에서는 신중히 사용

                    **성공 응답 예시:**
                    ```json
                    {
                      "status": "success",
                      "message": "리포트 생성이 완료되었습니다. (2025-01-15 ~ 2025-01-21)",
                      "data": null
                    }
                    ```
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @PostMapping("/generate-now")
    public ApiResponse<Void> generateReportsNow() {
        log.info(" [관리자 API] 리포트 수동 생성 요청");

        // 최근 7일 계산
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6); // D-6 ~ D-day (7일)

        // 리포트 레이블: "Demo-YYYY-MM-DD"
        String reportLabel = "Demo-" + today.toString();

        log.info(" 분석 기간: {} ~ {}", weekAgo, today);
        log.info(" 리포트 레이블: {}", reportLabel);

        try {
            // 리포트 생성 실행
            reportService.generateReports(weekAgo, today, reportLabel);

            String message = String.format("리포트 생성이 완료되었습니다. (%s ~ %s)", weekAgo, today);
            log.info("[관리자 API] {}", message);

            return ApiResponse.ok(null, message);

        } catch (Exception e) {
            log.error("[관리자 API] 리포트 생성 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.REPORT_GENERATION_FAIL, e.getMessage());
        }
    }

    @Operation(
            summary = "이번주 리포트 삭제 (테스트용)",
            description = """
                    현재 로그인한 사용자의 이번주 리포트를 삭제합니다.

                    **권한:** ADMIN만 접근 가능

                    **사용 목적:**
                    - 테스트 중 잘못 생성된 리포트 삭제
                    - 리포트 재생성 전 기존 데이터 제거

                    **주의사항:**
                    - "Demo-YYYY-MM-DD" 형식의 이번주 리포트만 삭제됩니다.
                    - 리포트가 없으면 404 에러가 발생합니다.

                    **성공 응답 예시:**
                    ```json
                    {
                      "status": "SUCCESS",
                      "message": "이번주 리포트가 삭제되었습니다.",
                      "data": null
                    }
                    ```

                    **에러 응답 (404 Not Found):**
                    ```json
                    {
                      "status": "FAIL",
                      "code": "REPORT-001",
                      "message": "이번주 리포트가 존재하지 않습니다."
                    }
                    ```
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @DeleteMapping("/current-week")
    public ApiResponse<Void> deleteCurrentWeekReport(
            @Parameter(hidden = true) @AuthenticationPrincipal Users user) {

        log.info("[관리자 API] 이번주 리포트 삭제 요청: 사용자 ID {}", user.getId());

        try {
            reportService.deleteCurrentWeekReport(user);
            String message = "이번주 리포트가 삭제되었습니다.";
            log.info("[관리자 API] {}", message);
            return ApiResponse.ok(null, message);

        } catch (CustomException e) {
            log.error("[관리자 API] 리포트 삭제 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[관리자 API] 리포트 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.DATA_DELETE_FAIL, e.getMessage());
        }
    }

    @Operation(
            summary = "특정 타입으로 리포트 생성 (테스트용)",
            description = """
                    현재 로그인한 사용자의 리포트를 지정된 타입으로 강제 생성합니다.
                    실제 데이터 분석 없이 원하는 페르소나 타입으로 리포트를 생성합니다.

                    **권한:** ADMIN만 접근 가능

                    **요청 본문:**
                    ```json
                    {
                      "reportType": "SCREENSHOT_MASTER"
                    }
                    ```

                    **사용 가능한 타입:**
                    - TAG_ONE_WELL: 태그한우물
                    - SCREENSHOT_MASTER: 스크린샷 장인
                    - OCTOPUS_COLLECTOR: 문어발 수집가
                    - TRIPITAKA_MASTER: 팔만대장경 장인
                    - NIGHT_OWL: 밤도깨비
                    - MIRACLE_MORNING_BEAR: 미라클 모닝곰
                    - NO_TYPE: 타입 없음

                    **사용 목적:**
                    - 프론트엔드 UI 테스트
                    - 각 타입별 화면 확인
                    - 데모 및 시연

                    **주의사항:**
                    - 이미 이번주 리포트가 존재하면 409 에러가 발생합니다.
                    - 먼저 DELETE /current-week로 삭제 후 다시 생성하세요.

                    **성공 응답 예시:**
                    ```json
                    {
                      "status": "SUCCESS",
                      "message": "SCREENSHOT_MASTER 타입으로 리포트가 생성되었습니다.",
                      "data": null
                    }
                    ```

                    **에러 응답 (409 Conflict):**
                    ```json
                    {
                      "status": "FAIL",
                      "code": "REPORT-002",
                      "message": "이번주 리포트가 이미 존재합니다. 먼저 삭제 후 다시 시도하세요."
                    }
                    ```
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @PostMapping("/generate-with-type")
    public ApiResponse<Void> generateReportWithType(
            @Valid @RequestBody GenerateWithTypeRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Users user) {

        log.info("[관리자 API] 특정 타입으로 리포트 생성 요청: 사용자 ID {}, 타입: {}",
                user.getId(), request.getReportType());

        try {
            reportService.generateReportWithType(user, request.getReportType());
            String message = String.format("%s 타입으로 리포트가 생성되었습니다.", request.getReportType());
            log.info("[관리자 API] {}", message);
            return ApiResponse.ok(null, message);

        } catch (CustomException e) {
            log.error("[관리자 API] 리포트 생성 실패: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error(" [관리자 API] 리포트 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.REPORT_GENERATION_FAIL, e.getMessage());
        }
    }
}

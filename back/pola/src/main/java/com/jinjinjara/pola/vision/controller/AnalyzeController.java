package com.jinjinjara.pola.vision.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.user.entity.Users;
import com.jinjinjara.pola.vision.dto.request.AnalyzeRequest;
import com.jinjinjara.pola.vision.dto.response.AnalyzeResponse;
import com.jinjinjara.pola.vision.service.AnalyzeFacadeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Analyze API", description = "S3 URL 기반 태그 추출 및 카테고리 분류 API")
@RestController
@RequestMapping("/api/v1/analyze")
@RequiredArgsConstructor
public class AnalyzeController {

    private final AnalyzeFacadeService analyzeFacadeService;

    @Operation(
            summary = "S3 URL 분석",
            description = "S3(또는 프리사인드) URL을 입력으로 받아 태그 추출 및 카테고리 분류를 수행합니다."
    )
    @PostMapping("/url")
    public ApiResponse<AnalyzeResponse> analyzeByUrl(
            @AuthenticationPrincipal @Parameter(hidden = true) Users user,
            @RequestBody AnalyzeRequest request
    ) {
        if (user == null || user.getId() == null) {
            throw new CustomException(ErrorCode.USER_UNAUTHORIZED, "로그인이 필요합니다.");
        }
        if (request == null || !StringUtils.hasText(request.getS3Url())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "url이 비어 있습니다.");
        }

        AnalyzeResponse data = analyzeFacadeService.analyze(user.getId(), request.getS3Url());
        return ApiResponse.ok(data, "분석이 완료되었습니다.");
    }
}

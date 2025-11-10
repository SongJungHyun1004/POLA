package com.jinjinjara.pola.search.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.s3.service.S3Service;
import com.jinjinjara.pola.search.model.FileSearch;
import com.jinjinjara.pola.search.service.FileSearchService;
import com.jinjinjara.pola.user.entity.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "검색 API", description = "OpenSearch 기반 파일 검색 API")
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class FileSearchController {

    private final FileSearchService service;
    private final S3Service s3Service;

    // ========== 기존 관리용 API (내부 사용) ==========

    @Operation(
            summary = "[내부] 검색 문서 저장",
            description = """
                    OpenSearch에 파일 정보를 색인합니다. (내부 관리용 API)

                    **사용 목적:**
                    - 파일 업로드/수정 시 자동으로 호출되어 검색 인덱스를 업데이트합니다.
                    - 직접 호출할 필요가 없으며, DataService에서 자동으로 처리됩니다.

                    **요청 본문 예시:**
                    ```json
                    {
                      "fileId": 123,
                      "userId": 1,
                      "categoryName": "개발",
                      "tags": "React, TypeScript",
                      "context": "리액트 컴포넌트 설계 문서",
                      "ocrText": "추출된 텍스트",
                      "imageUrl": "s3://bucket/path/to/file.jpg",
                      "createdAt": "2025-01-15T10:30:00"
                    }
                    ```
                    """
    )
    @PostMapping("/index")
    public ApiResponse<Void> save(@RequestBody FileSearch file) {
        try {
            service.save(file);
            return ApiResponse.success("색인 완료");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SEARCH_FAIL, e.getMessage());
        }
    }

    @Operation(summary = "[내부] 검색 문서 조회", description = "OpenSearch에서 파일 정보를 조회합니다 (내부용)")
    @GetMapping("/index/{id}")
    public ApiResponse<FileSearch> get(@PathVariable Long id) {
        try {
            FileSearch result = service.get(id);
            return ApiResponse.ok(result, "조회 완료");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SEARCH_FAIL, e.getMessage());
        }
    }

    @Operation(summary = "[내부] 검색 문서 삭제", description = "OpenSearch에서 파일 정보를 삭제합니다 (내부용)")
    @DeleteMapping("/index/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ApiResponse.success("삭제 완료");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SEARCH_FAIL, e.getMessage());
        }
    }

    // ========== 사용자용 검색 API ==========

    @Operation(
            summary = "태그 검색",
            description = """
                    특정 태그로 파일을 검색합니다.

                    **검색 방식:**
                    - 파일에 연결된 태그 중 검색어와 일치하는 태그가 있는 파일을 반환합니다.
                    - 로그인한 사용자의 파일만 검색됩니다.

                    **인증 방식:**
                    - Authorization 헤더에 JWT Access Token 필요

                    **요청 예시:**
                    ```
                    GET /api/v1/search/tags?tag=여행
                    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
                    ```

                    **성공 응답 (200 OK):**
                    ```json
                    {
                      "status": "success",
                      "message": "태그 검색 완료",
                      "data": [
                        {
                          "fileId": 123,
                          "userId": 1,
                          "categoryName": "사진",
                          "tags": "여행, 제주도, 바다",
                          "context": "제주도 여행 사진",
                          "ocrText": "",
                          "imageUrl": "https://presigned-url.s3.amazonaws.com/...",
                          "createdAt": "2025-01-15T10:30:00"
                        }
                      ]
                    }
                    ```

                    **참고:**
                    - imageUrl은 실제 접근 가능한 Presigned URL로 반환됩니다.
                    - Presigned URL은 일정 시간 후 만료됩니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/tags")
    public ApiResponse<List<FileSearch>> searchByTag(
            @Parameter(hidden = true) @AuthenticationPrincipal Users user,
            @Parameter(description = "검색할 태그", example = "여행", required = true)
            @RequestParam String tag
    ) {
        try {
            List<FileSearch> results = service.searchByTag(user.getId(), tag);
            List<FileSearch> withPresignedUrls = addPresignedUrls(results);
            return ApiResponse.ok(withPresignedUrls, "태그 검색 완료");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SEARCH_FAIL, e.getMessage());
        }
    }

    @Operation(
            summary = "카테고리 검색",
            description = """
                    특정 카테고리의 파일을 검색합니다.

                    **검색 방식:**
                    - AI가 자동으로 분류한 카테고리 또는 사용자가 지정한 카테고리로 검색합니다.
                    - 로그인한 사용자의 파일만 검색됩니다.

                    **인증 방식:**
                    - Authorization 헤더에 JWT Access Token 필요

                    **요청 예시:**
                    ```
                    GET /api/v1/search/category?categoryName=개발
                    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
                    ```

                    **성공 응답 (200 OK):**
                    ```json
                    {
                      "status": "success",
                      "message": "카테고리 검색 완료",
                      "data": [
                        {
                          "fileId": 456,
                          "userId": 1,
                          "categoryName": "개발",
                          "tags": "React, TypeScript, 컴포넌트",
                          "context": "리액트 컴포넌트 설계 문서",
                          "ocrText": "Component Design Patterns",
                          "imageUrl": "https://presigned-url.s3.amazonaws.com/...",
                          "createdAt": "2025-01-14T15:20:00"
                        }
                      ]
                    }
                    ```
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/category")
    public ApiResponse<List<FileSearch>> searchByCategory(
            @Parameter(hidden = true) @AuthenticationPrincipal Users user,
            @Parameter(description = "검색할 카테고리명", example = "개발", required = true)
            @RequestParam String categoryName
    ) {
        try {
            List<FileSearch> results = service.searchByCategoryName(user.getId(), categoryName);
            return ApiResponse.ok(results, "카테고리 검색 완료");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SEARCH_FAIL, e.getMessage());
        }
    }

    @Operation(
            summary = "OCR 텍스트 검색",
            description = """
                    이미지에서 OCR로 추출된 텍스트 내용을 검색합니다.

                    **검색 방식:**
                    - Google Cloud Vision API로 추출된 텍스트에서 키워드를 검색합니다.
                    - 부분 일치 검색을 지원합니다. (예: "API"로 검색 시 "API 키", "REST API" 등 매칭)
                    - 로그인한 사용자의 파일만 검색됩니다.

                    **인증 방식:**
                    - Authorization 헤더에 JWT Access Token 필요

                    **요청 예시:**
                    ```
                    GET /api/v1/search/ocr?keyword=API 키
                    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
                    ```

                    **성공 응답 (200 OK):**
                    ```json
                    {
                      "status": "success",
                      "message": "OCR 텍스트 검색 완료",
                      "data": [
                        {
                          "fileId": 789,
                          "userId": 1,
                          "categoryName": "개발",
                          "tags": "문서, 설정",
                          "context": "환경 설정 스크린샷",
                          "ocrText": "Google API 키: AIzaSyC...",
                          "imageUrl": "https://presigned-url.s3.amazonaws.com/...",
                          "createdAt": "2025-01-13T09:15:00"
                        }
                      ]
                    }
                    ```

                    **참고:**
                    - OCR 텍스트가 없는 파일은 검색 결과에 포함되지 않습니다.
                    - 현재 OCR 기능은 사용되지 않으며, ocrText는 빈 값입니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/ocr")
    public ApiResponse<List<FileSearch>> searchByOcrText(
            @Parameter(hidden = true) @AuthenticationPrincipal Users user,
            @Parameter(description = "검색할 키워드", example = "API 키", required = true)
            @RequestParam String keyword
    ) {
        try {
            List<FileSearch> results = service.searchByOcrText(user.getId(), keyword);
            List<FileSearch> withPresignedUrls = addPresignedUrls(results);
            return ApiResponse.ok(withPresignedUrls, "OCR 텍스트 검색 완료");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SEARCH_FAIL, e.getMessage());
        }
    }

    @Operation(
            summary = "설명 검색",
            description = """
                    AI가 생성한 파일 설명(context)에서 키워드를 검색합니다.

                    **검색 방식:**
                    - Gemini Vision AI가 생성한 이미지 설명 텍스트에서 키워드를 검색합니다.
                    - 부분 일치 검색을 지원합니다. (예: "버튼"으로 검색 시 "파란색 버튼", "로그인 버튼" 등 매칭)
                    - 로그인한 사용자의 파일만 검색됩니다.

                    **context 필드란?**
                    - 파일 업로드 시 AI가 자동으로 생성하는 이미지 설명입니다.
                    - 예: "리액트 컴포넌트 설계 다이어그램", "제주도 해변 풍경 사진" 등

                    **인증 방식:**
                    - Authorization 헤더에 JWT Access Token 필요

                    **요청 예시:**
                    ```
                    GET /api/v1/search/context?keyword=파란색 버튼
                    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
                    ```

                    **성공 응답 (200 OK):**
                    ```json
                    {
                      "status": "success",
                      "message": "설명 검색 완료",
                      "data": [
                        {
                          "fileId": 234,
                          "userId": 1,
                          "categoryName": "디자인",
                          "tags": "UI, 버튼, 컴포넌트",
                          "context": "파란색 로그인 버튼 디자인 시안",
                          "ocrText": "",
                          "imageUrl": "https://presigned-url.s3.amazonaws.com/...",
                          "createdAt": "2025-01-12T14:30:00"
                        }
                      ]
                    }
                    ```
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/context")
    public ApiResponse<List<FileSearch>> searchByContext(
            @Parameter(hidden = true) @AuthenticationPrincipal Users user,
            @Parameter(description = "검색할 키워드", example = "파란색 버튼", required = true)
            @RequestParam String keyword
    ) {
        try {
            List<FileSearch> results = service.searchByContext(user.getId(), keyword);
            List<FileSearch> withPresignedUrls = addPresignedUrls(results);
            return ApiResponse.ok(withPresignedUrls, "설명 검색 완료");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SEARCH_FAIL, e.getMessage());
        }
    }

    @Operation(
            summary = "통합 검색",
            description = """
                    태그, OCR 텍스트, 설명(context)을 모두 검색하여 종합적인 검색 결과를 제공합니다.

                    **검색 방식:**
                    - 다음 필드들을 모두 검색하여 하나라도 매칭되면 결과에 포함됩니다:
                      1. **tags**: 파일에 연결된 태그
                      2. **ocrText**: OCR로 추출된 텍스트 (현재 사용 안 함)
                      3. **context**: AI가 생성한 이미지 설명
                    - 로그인한 사용자의 파일만 검색됩니다.
                    - 중복 제거 처리됩니다.

                    **사용 시나리오:**
                    - "개발"로 검색 시:
                      - tags에 "개발" 포함 → 매칭
                      - context에 "개발 문서" 포함 → 매칭
                      - ocrText에 "개발 가이드" 포함 → 매칭

                    **인증 방식:**
                    - Authorization 헤더에 JWT Access Token 필요

                    **요청 예시:**
                    ```
                    GET /api/v1/search/all?keyword=개발
                    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
                    ```

                    **성공 응답 (200 OK):**
                    ```json
                    {
                      "status": "success",
                      "message": "통합 검색 완료",
                      "data": [
                        {
                          "fileId": 123,
                          "userId": 1,
                          "categoryName": "개발",
                          "tags": "React, 개발, 프론트엔드",
                          "context": "리액트 개발 가이드 문서",
                          "ocrText": "",
                          "imageUrl": "https://presigned-url.s3.amazonaws.com/...",
                          "createdAt": "2025-01-15T10:30:00"
                        },
                        {
                          "fileId": 456,
                          "userId": 1,
                          "categoryName": "문서",
                          "tags": "가이드, 문서",
                          "context": "개발 환경 설정 가이드",
                          "ocrText": "",
                          "imageUrl": "https://presigned-url.s3.amazonaws.com/...",
                          "createdAt": "2025-01-14T09:20:00"
                        }
                      ]
                    }
                    ```

                    **참고:**
                    - 가장 포괄적인 검색 방식으로, 정확한 매칭이 필요한 경우 개별 검색 API를 사용하세요.
                    - 태그만 검색: `/tags`
                    - 설명만 검색: `/context`
                    - OCR만 검색: `/ocr`
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/all")
    public ApiResponse<List<FileSearch>> searchAll(
            @Parameter(hidden = true) @AuthenticationPrincipal Users user,
            @Parameter(description = "검색할 키워드", example = "개발", required = true)
            @RequestParam String keyword
    ) {
        try {
            List<FileSearch> results = service.searchAll(user.getId(), keyword);
            List<FileSearch> withPresignedUrls = addPresignedUrls(results);
            return ApiResponse.ok(withPresignedUrls, "통합 검색 완료");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SEARCH_FAIL, e.getMessage());
        }
    }

    /**
     * 검색 결과에 Presigned URL 추가
     * S3 키를 실제로 접근 가능한 URL로 변환
     */
    private List<FileSearch> addPresignedUrls(List<FileSearch> searches) {
        return searches.stream()
                .map(search -> {
                    try {
                        if (search.getImageUrl() != null && !search.getImageUrl().startsWith("http")) {
                            // S3 키를 presigned URL로 변환
                            URL presignedUrl = s3Service.generateDownloadUrl(search.getImageUrl());
                            search.setImageUrl(presignedUrl.toString());
                        }
                    } catch (Exception e) {
                        log.warn("Presigned URL 생성 실패: fileId={}, key={}",
                                search.getFileId(), search.getImageUrl(), e);
                    }
                    return search;
                })
                .collect(Collectors.toList());
    }
}
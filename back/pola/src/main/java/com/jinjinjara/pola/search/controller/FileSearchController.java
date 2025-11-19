package com.jinjinjara.pola.search.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.s3.service.S3Service;
import com.jinjinjara.pola.search.model.FileSearch;
import com.jinjinjara.pola.search.model.SearchResponse;
import com.jinjinjara.pola.search.model.TagSuggestionResponse;
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
            summary = "태그 검색 (파일 전체 반환)",
            description = """
                    특정 태그로 파일을 검색합니다.

                    **검색 방식:**
                    - 파일에 연결된 태그 중 검색어와 일치하거나 포함하는 태그가 있는 파일을 반환합니다.
                    - 로그인한 사용자의 파일만 검색됩니다.
                    - **한 글자 검색 지원**: "립", "리" 등 한 글자도 검색 가능합니다 (Edge N-gram 사용).
                    - **부분 검색 지원**: "강아"로 검색 시 "강아지" 등 접두사 매칭됩니다.

                    **검색 전략 (Nori + Edge N-gram):**
                    1. **단일 단어** (예: "립", "강아지"):
                       - Edge N-gram으로 한 글자 및 접두사 검색 지원
                       - "립" → "립", "립스틱", "립밤" 등 매칭

                    2. **복합어** (예: "강아지잠옷", "React개발"):
                       - Nori 형태소 분석으로 토큰 분해: "강아지잠옷" → ["강아지", "잠옷"]
                       - **모든 토큰이 포함된 파일만 반환** (AND 조건)
                       - "강아지잠옷" 검색 시 → "강아지" AND "잠옷" 모두 있어야 매칭
                       - 과다 매칭 방지: "고양이잠옷"은 제외됨 ("강아지" 없음)

                    **태그 목록만 필요한 경우:**
                    - 자동완성 기능을 위해서는 `/tag-suggestions` API를 사용하세요.

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
                      "data": {
                        "totalCount": 1,
                        "results": [
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
                    }
                    ```

                    **참고:**
                    - imageUrl은 실제 접근 가능한 Presigned URL로 반환됩니다.
                    - Presigned URL은 일정 시간 후 만료됩니다.
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/tags")
    public ApiResponse<SearchResponse> searchByTag(
            @Parameter(hidden = true) @AuthenticationPrincipal Users user,
            @Parameter(description = "검색할 태그", example = "여행", required = true)
            @RequestParam String tag
    ) {
        try {
            List<FileSearch> results = service.searchByTag(user.getId(), tag);
            List<FileSearch> withPresignedUrls = addPresignedUrls(results);
            SearchResponse response = SearchResponse.from(withPresignedUrls);
            return ApiResponse.ok(response, "태그 검색 완료");
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
                    - **정확히 일치하는 카테고리명**으로만 검색됩니다 (부분 검색 불가).
                    - 로그인한 사용자의 파일만 검색됩니다.

                    **카테고리 예시:**
                    - "개발", "디자인", "사진", "문서", "영수증", "명함" 등

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
                      "data": {
                        "totalCount": 1,
                        "results": [
                          {
                            "fileId": 456,
                            "userId": 1,
                            "categoryName": "개발",
                            "tags": "React, TypeScript, 컴포넌트",
                            "context": "리액트 컴포넌트 설계 문서",
                            "ocrText": "Component Design Patterns...",
                            "imageUrl": "https://presigned-url.s3.amazonaws.com/...",
                            "createdAt": "2025-01-14T15:20:00"
                          }
                        ]
                      }
                    }
                    ```

                    **참고:**
                    - 카테고리명은 정확히 입력해야 합니다 ("개발" ≠ "개").
                    - 키워드 검색이 필요한 경우 통합 검색(`/all`)을 사용하세요.
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/category")
    public ApiResponse<SearchResponse> searchByCategory(
            @Parameter(hidden = true) @AuthenticationPrincipal Users user,
            @Parameter(description = "검색할 카테고리명", example = "개발", required = true)
            @RequestParam String categoryName
    ) {
        try {
            List<FileSearch> results = service.searchByCategoryName(user.getId(), categoryName);
            List<FileSearch> withPresignedUrls = addPresignedUrls(results);
            SearchResponse response = SearchResponse.from(withPresignedUrls);
            return ApiResponse.ok(response, "카테고리 검색 완료");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SEARCH_FAIL, e.getMessage());
        }
    }

    @Operation(
            summary = "OCR 텍스트 검색",
            description = """
                    이미지 및 텍스트 파일에서 추출된 텍스트 내용을 검색합니다.

                    **검색 방식:**
                    - Google Cloud Vision API로 추출된 이미지 텍스트에서 키워드를 검색합니다.
                    - **텍스트 파일(.txt 등)의 전체 내용**도 ocrText 필드에 저장되어 검색 가능합니다.
                    - Nori 형태소 분석 + Edge N-gram 조합으로 정확한 검색을 지원합니다.
                    - 로그인한 사용자의 파일만 검색됩니다.

                    **검색 전략:**
                    1. **단일 단어** (예: "API", "설정"):
                       - Edge N-gram으로 부분 매칭
                       - "API"로 검색 시 "API 키", "REST API" 등 매칭

                    2. **복합어** (예: "환경설정"):
                       - Nori 형태소 분석: "환경설정" → ["환경", "설정"]
                       - 모든 토큰이 포함된 파일만 반환 (AND 조건)

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
                    - ocrText가 없는 파일은 검색 결과에 포함되지 않습니다.
                    - 텍스트 파일의 풀텍스트 검색에 매우 유용합니다.
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/ocr")
    public ApiResponse<SearchResponse> searchByOcrText(
            @Parameter(hidden = true) @AuthenticationPrincipal Users user,
            @Parameter(description = "검색할 키워드", example = "API 키", required = true)
            @RequestParam String keyword
    ) {
        try {
            List<FileSearch> results = service.searchByOcrText(user.getId(), keyword);
            List<FileSearch> withPresignedUrls = addPresignedUrls(results);
            SearchResponse response = SearchResponse.from(withPresignedUrls);
            return ApiResponse.ok(response, "OCR 텍스트 검색 완료");
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
                    - Nori 형태소 분석 + Edge N-gram 조합으로 정확한 검색을 지원합니다.
                    - 로그인한 사용자의 파일만 검색됩니다.

                    **context 필드란?**
                    - 파일 업로드 시 AI가 자동으로 생성하는 이미지 설명입니다.
                    - 예: "리액트 컴포넌트 설계 다이어그램", "제주도 해변 풍경 사진" 등

                    **검색 전략:**
                    1. **단일 단어** (예: "버튼", "풍경"):
                       - Edge N-gram으로 부분 매칭
                       - "버튼"으로 검색 시 "파란색 버튼", "로그인 버튼" 등 매칭

                    2. **복합어** (예: "로그인버튼"):
                       - Nori 형태소 분석: "로그인버튼" → ["로그인", "버튼"]
                       - 모든 토큰이 포함된 설명만 반환 (AND 조건)

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
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/context")
    public ApiResponse<SearchResponse> searchByContext(
            @Parameter(hidden = true) @AuthenticationPrincipal Users user,
            @Parameter(description = "검색할 키워드", example = "파란색 버튼", required = true)
            @RequestParam String keyword
    ) {
        try {
            List<FileSearch> results = service.searchByContext(user.getId(), keyword);
            List<FileSearch> withPresignedUrls = addPresignedUrls(results);
            SearchResponse response = SearchResponse.from(withPresignedUrls);
            return ApiResponse.ok(response, "설명 검색 완료");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SEARCH_FAIL, e.getMessage());
        }
    }

    @Operation(
            summary = "통합 검색 (추천)",
            description = """
                    태그, OCR 텍스트, 설명(context)을 모두 검색하여 종합적인 검색 결과를 제공합니다.

                    **검색 방식:**
                    - 다음 필드들을 모두 검색합니다:
                      1. **tags**: 파일에 연결된 태그
                      2. **ocrText**: OCR로 추출된 텍스트 + 텍스트 파일의 전체 내용
                      3. **context**: AI가 생성한 이미지 설명
                    - 로그인한 사용자의 파일만 검색됩니다.
                    - **한 글자 검색 지원**: "립", "리" 등 한 글자도 검색 가능합니다 (Edge N-gram).

                    **검색 전략 (Nori + Edge N-gram):**
                    1. **단일 단어** (예: "개발", "API"):
                       - 세 필드 중 하나라도 키워드가 포함되면 매칭 (OR 조건)
                       - Edge N-gram으로 부분 매칭 지원
                       - "개발" 검색 시:
                         - tags에 "개발" 포함 → 매칭
                         - context에 "개발 문서" 포함 → 매칭
                         - ocrText에 "개발 가이드" 포함 → 매칭

                    2. **복합어** (예: "강아지잠옷", "React개발"):
                       - Nori 형태소 분석: "강아지잠옷" → ["강아지", "잠옷"]
                       - **각 토큰이 최소 하나의 필드에는 반드시 존재해야 함** (AND 조건)
                       - "강아지잠옷" 검색 시:
                         - ✅ tags: "강아지", ocrText: "잠옷" → 매칭 (각 토큰이 어딘가에 있음)
                         - ✅ tags: "강아지잠옷" → 매칭 (둘 다 tags에 있음)
                         - ❌ tags: "고양이", ocrText: "잠옷" → 제외 ("강아지" 없음)
                       - 과다 매칭 방지로 정확한 검색 결과 제공

                    **사용 시나리오:**
                    - 가장 포괄적이고 정확한 검색 방식입니다.
                    - 파일이 어디에 저장되었는지 모를 때 (태그인지, 텍스트인지, 설명인지)
                    - 텍스트 파일의 내용까지 검색하고 싶을 때

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
                          "ocrText": "React 컴포넌트 개발 패턴...",
                          "imageUrl": "https://presigned-url.s3.amazonaws.com/...",
                          "createdAt": "2025-01-15T10:30:00"
                        },
                        {
                          "fileId": 456,
                          "userId": 1,
                          "categoryName": "문서",
                          "tags": "가이드, 문서",
                          "context": "개발 환경 설정 가이드",
                          "ocrText": "개발 환경 설정 방법:\n1. Node.js 설치...",
                          "imageUrl": "https://presigned-url.s3.amazonaws.com/...",
                          "createdAt": "2025-01-14T09:20:00"
                        }
                      ]
                    }
                    ```

                    **참고:**
                    - 특정 필드만 검색하고 싶은 경우:
                      - 태그만 검색: `/tags`
                      - 설명만 검색: `/context`
                      - OCR만 검색: `/ocr`
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/all")
    public ApiResponse<SearchResponse> searchAll(
            @Parameter(hidden = true) @AuthenticationPrincipal Users user,
            @Parameter(description = "검색할 키워드", example = "개발", required = true)
            @RequestParam String keyword
    ) {
        try {
            List<FileSearch> results = service.searchAll(user.getId(), keyword);
            List<FileSearch> withPresignedUrls = addPresignedUrls(results);
            SearchResponse response = SearchResponse.from(withPresignedUrls);
            return ApiResponse.ok(response, "통합 검색 완료");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SEARCH_FAIL, e.getMessage());
        }
    }

    @Operation(
            summary = "태그 자동완성 (실시간)",
            description = """
                    입력한 키워드로 시작하거나 포함하는 태그 목록을 반환합니다.

                    **검색 방식:**
                    - **한 글자 검색 지원**: "립", "리" 등 한 글자도 검색 가능합니다 (Edge N-gram).
                    - **접두사 검색**: "리"로 검색 시 "립", "립스틱", "리액트" 등 접두사로 시작하는 태그 매칭.
                    - **부분 검색**: Wildcard로 중간 매칭도 지원 (예: "액트" → "리액트").
                    - 로그인한 사용자가 사용한 태그만 검색됩니다.
                    - 중복 제거 후 알파벳순으로 정렬하여 반환됩니다.

                    **검색 전략 (Edge N-gram + Wildcard):**
                    - Edge N-gram: 접두사 검색 최적화 (1~10 글자)
                    - Wildcard: 중간 매칭 지원 (fallback)
                    - 예시:
                      - "리" 입력 → ["립", "립스틱", "리액트"] (접두사 매칭)
                      - "강아" 입력 → ["강아지", "강아지잠옷"] (접두사 매칭)

                    **사용 목적:**
                    - 태그 입력 시 자동완성 기능 제공
                    - 검색 중 연관 태그 추천
                    - 실시간 검색 UI 지원

                    **인증 방식:**
                    - Authorization 헤더에 JWT Access Token 필요

                    **요청 예시:**
                    ```
                    GET /api/v1/search/tag-suggestions?keyword=리
                    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
                    ```

                    **성공 응답 (200 OK):**
                    ```json
                    {
                      "status": "SUCCESS",
                      "message": "태그 자동완성 조회 완료",
                      "data": {
                        "tags": ["립", "립스틱", "리액트", "리팩토링"],
                        "count": 4
                      }
                    }
                    ```

                    **빈 결과 응답 (200 OK):**
                    ```json
                    {
                      "status": "SUCCESS",
                      "message": "태그 자동완성 조회 완료",
                      "data": {
                        "tags": [],
                        "count": 0
                      }
                    }
                    ```

                    **참고:**
                    - 전체 파일 검색이 아닌 태그 목록만 반환됩니다 (경량 응답).
                    - 실시간 자동완성에 최적화되어 있습니다 (~12ms).
                    - 파일 전체 정보가 필요하면 `/tags` API를 사용하세요.
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/tag-suggestions")
    public ApiResponse<TagSuggestionResponse> searchTagSuggestions(
            @Parameter(hidden = true) @AuthenticationPrincipal Users user,
            @Parameter(description = "검색할 키워드 (한 글자 이상)", example = "리", required = true)
            @RequestParam String keyword
    ) {
        try {
            List<String> tags = service.searchTagSuggestions(user.getId(), keyword);
            TagSuggestionResponse response = TagSuggestionResponse.from(tags);
            return ApiResponse.ok(response, "태그 자동완성 조회 완료");
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
                            URL previewUrl = s3Service.generatePreviewUrl(
                                    search.getImageUrl(),
                                    search.getFileType()
                            );
                            search.setImageUrl(previewUrl.toString());
                        }
                    } catch (Exception e) {
                        log.warn("Preview URL 생성 실패: fileId={}, key={}",
                                search.getFileId(), search.getImageUrl(), e);
                    }
                    return search;
                })
                .collect(Collectors.toList());
    }

}
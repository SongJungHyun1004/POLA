package com.jinjinjara.pola.search.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.search.model.FileSearch;
import com.jinjinjara.pola.search.service.FileSearchService;
import com.jinjinjara.pola.user.entity.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Tag(name = "검색 API", description = "OpenSearch 기반 파일 검색 API")
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class FileSearchController {

    private final FileSearchService service;

    // ========== 기존 관리용 API (내부 사용) ==========

    @Operation(summary = "[내부] 검색 문서 저장", description = "OpenSearch에 파일 정보를 색인합니다 (내부용)")
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

    @Operation(summary = "태그 검색", description = "특정 태그로 파일을 검색합니다.")
    @GetMapping("/tags")
    public ApiResponse<List<FileSearch>> searchByTag(
            @AuthenticationPrincipal Users user,
            @Parameter(description = "검색할 태그", example = "여행")
            @RequestParam String tag
    ) {
        try {
            List<FileSearch> results = service.searchByTag(user.getId(), tag);
            return ApiResponse.ok(results, "태그 검색 완료");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SEARCH_FAIL, e.getMessage());
        }
    }

    @Operation(summary = "카테고리 검색", description = "특정 카테고리의 파일을 검색합니다.")
    @GetMapping("/category")
    public ApiResponse<List<FileSearch>> searchByCategory(
            @AuthenticationPrincipal Users user,
            @Parameter(description = "검색할 카테고리명", example = "개발")
            @RequestParam String categoryName
    ) {
        try {
            List<FileSearch> results = service.searchByCategoryName(user.getId(), categoryName);
            return ApiResponse.ok(results, "카테고리 검색 완료");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SEARCH_FAIL, e.getMessage());
        }
    }

    @Operation(summary = "OCR 텍스트 검색", description = "OCR로 추출된 텍스트에서 키워드를 검색합니다.")
    @GetMapping("/ocr")
    public ApiResponse<List<FileSearch>> searchByOcrText(
            @AuthenticationPrincipal Users user,
            @Parameter(description = "검색할 키워드", example = "API 키")
            @RequestParam String keyword
    ) {
        try {
            List<FileSearch> results = service.searchByOcrText(user.getId(), keyword);
            return ApiResponse.ok(results, "OCR 텍스트 검색 완료");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SEARCH_FAIL, e.getMessage());
        }
    }

    @Operation(summary = "설명 검색", description = "파일 설명(context)에서 키워드를 검색합니다.")
    @GetMapping("/context")
    public ApiResponse<List<FileSearch>> searchByContext(
            @AuthenticationPrincipal Users user,
            @Parameter(description = "검색할 키워드", example = "파란색 버튼")
            @RequestParam String keyword
    ) {
        try {
            List<FileSearch> results = service.searchByContext(user.getId(), keyword);
            return ApiResponse.ok(results, "설명 검색 완료");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SEARCH_FAIL, e.getMessage());
        }
    }

    @Operation(summary = "통합 검색", description = "태그, OCR 텍스트, 설명을 모두 검색합니다.")
    @GetMapping("/all")
    public ApiResponse<List<FileSearch>> searchAll(
            @AuthenticationPrincipal Users user,
            @Parameter(description = "검색할 키워드", example = "개발")
            @RequestParam String keyword
    ) {
        try {
            List<FileSearch> results = service.searchAll(user.getId(), keyword);
            return ApiResponse.ok(results, "통합 검색 완료");
        } catch (IOException e) {
            throw new CustomException(ErrorCode.SEARCH_FAIL, e.getMessage());
        }
    }
}
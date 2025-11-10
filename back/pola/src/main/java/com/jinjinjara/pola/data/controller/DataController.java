package com.jinjinjara.pola.data.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.common.dto.FileResponseDto;
import com.jinjinjara.pola.common.dto.PageRequestDto;
import com.jinjinjara.pola.common.dto.PagedResponseDto;
import com.jinjinjara.pola.data.dto.request.FileShareRequest;
import com.jinjinjara.pola.data.dto.request.FileUpdateRequest;
import com.jinjinjara.pola.data.dto.request.FileUploadCompleteRequest;
import com.jinjinjara.pola.data.dto.response.*;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.service.DataService;
import com.jinjinjara.pola.user.dto.response.UserInfoResponse;
import com.jinjinjara.pola.user.entity.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Data API", description = "파일 데이터 관리 API (업로드, 카테고리 변경, 즐겨찾기 등)")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class DataController {

    private final DataService dataService;

    // ==========================================
    // Presigned 업로드 완료
    // ==========================================
    @Operation(
            summary = "파일 업로드 완료 처리",
            description = "클라이언트에서 Presigned URL로 S3 업로드가 끝난 후, 해당 파일 메타데이터를 DB에 저장합니다.\n" +
                    "업로드된 URL의 '?' 앞부분을 originUrl로 전달해야 합니다."
    )
    @PostMapping("/complete")
    public ApiResponse<File> saveUploadedFile(
            @AuthenticationPrincipal Users user,
            @RequestBody FileUploadCompleteRequest request
    ) {
        if (user == null) {
            throw new RuntimeException("인증 정보가 유효하지 않습니다.");
            // TODO: 추후 ErrorCode 기반 예외 처리
        }

        File savedFile = dataService.saveUploadedFile(user, request);
        return ApiResponse.ok(savedFile, "파일이 성공적으로 등록되었습니다.");
    }

    // ==========================================
    // 카테고리 변경
    // ==========================================
    @Operation(summary = "파일 카테고리 변경", description = "지정된 파일의 카테고리를 변경합니다.")
    @PutMapping("/{fileId}/category")
    public ApiResponse<File> updateFileCategory(
            @AuthenticationPrincipal Users user,
            @Parameter(description = "파일 ID", example = "10") @PathVariable Long fileId,
            @Parameter(description = "새 카테고리 ID", example = "3") @RequestParam Long categoryId
    ) {
        File updated = dataService.updateFileCategory(fileId, categoryId, user);
        return ApiResponse.ok(updated, "파일의 카테고리가 성공적으로 변경되었습니다.");
    }

    // ==========================================
    // 즐겨찾기 추가
    // ==========================================
    @Operation(summary = "파일 즐겨찾기 추가", description = "파일을 즐겨찾기로 등록합니다.")
    @PutMapping("/{fileId}/favorite")
    public ApiResponse<File> addFavorite(
            @AuthenticationPrincipal Users user,
            @Parameter(description = "파일 ID", example = "101") @PathVariable Long fileId,
            @Parameter(description = "즐겨찾기 정렬 순서", example = "1") @RequestParam(required = false) Integer sortValue
    ) {
        File updated = dataService.addFavorite(fileId, sortValue, user);
        return ApiResponse.ok(updated, "파일이 즐겨찾기에 추가되었습니다.");
    }

    // ==========================================
    // 즐겨찾기 제거
    // ==========================================
    @Operation(summary = "파일 즐겨찾기 제거", description = "해당 파일을 즐겨찾기에서 해제합니다.")
    @DeleteMapping("/{fileId}/favorite")
    public ApiResponse<File> removeFavorite(
            @AuthenticationPrincipal Users user,
            @Parameter(description = "파일 ID", example = "101") @PathVariable Long fileId
    ) {
        File updated = dataService.removeFavorite(fileId, user);
        return ApiResponse.ok(updated, "파일이 즐겨찾기에서 제거되었습니다.");
    }//    // 즐겨찾기된 파일 조회
//    @Operation(summary = "즐겨찾기된 파일 조회", description = "현재 로그인된 유저의 즐겨찾기 파일 목록을 조회합니다.")
//    @GetMapping("/favorites")
//    public ApiResponse<List<File>> getFavoriteFiles(@AuthenticationPrincipal Users user) {
//        List<File> favorites = dataService.getFavoriteFiles(user);
//        return ApiResponse.ok(favorites, "즐겨찾기된 파일 목록 조회 성공");
//    }

    /**
     * 리마인드 목록 조회
     * - 최근 7일 이내에 보지 않은 파일 중
     * - 조회수가 낮고 오래된 순으로 30개
     */
    @Operation(
            summary = "리마인드 파일 목록 조회",
            description = """
            최근 7일 이내에 보지 않은 파일 중 조회수가 낮고 오래된 순으로 30개를 반환합니다.
            리마인드는 사용자가 잊고 있던 데이터를 다시 상기시켜주는 용도입니다.
            """
    )
    @GetMapping("/reminders")
    public ApiResponse<List<DataResponse>> getRemindFiles(
            @AuthenticationPrincipal Users user
    ) {
        List<DataResponse> responses = dataService.getRemindFiles(user.getId());
        return ApiResponse.ok(responses, "리마인드 목록을 불러왔습니다.");
    }

    /**
     * 파일 단건 상세 조회
     * - 조회수 증가 + 마지막 열람 시각 업데이트 포함
     */
    @Operation(
            summary = "파일 상세 정보 조회",
            description = """
            파일 단건의 상세 정보를 반환합니다.
            호출 시 해당 파일의 조회수가 1 증가하고, 마지막 열람 시각(lastViewedAt)이 갱신됩니다.
            """
    )
    @GetMapping("/{fileId}")
    public ApiResponse<FileDetailResponse> getFileDetail(
            @AuthenticationPrincipal Users user,
            @PathVariable Long fileId
    ) {
        FileDetailResponse response = dataService.getFileDetail(user.getId(), fileId);
        return ApiResponse.ok(response, "파일 상세 정보를 불러왔습니다.");
    }

    @Operation(summary = "즐겨찾기 순서 변경", description = "특정 파일의 즐겨찾기 순서를 변경합니다.\n" +
            "예: 파일 5번을 2~3 사이에 넣으면 3번이 되고, 나머지는 한 칸씩 밀립니다. 앞이아닌 뒤에끼어넣기도 가능")
    @PutMapping("/{fileId}/sort")
    public ApiResponse<File> updateFavoriteSort(
            @AuthenticationPrincipal Users user,
            @Parameter(description = "파일 ID", example = "5") @PathVariable Long fileId,
            @Parameter(description = "새로운 정렬 순서", example = "3") @RequestParam int newSort
    ) {
        File updated = dataService.updateFavoriteSort(fileId, newSort, user);
        return ApiResponse.ok(updated, "즐겨찾기 순서 변경 완료");
    }
    // ==========================================
    // 파일 삭제 (임시)
    // ==========================================
    @Operation(summary = "데이터 삭제", description = "사용자가 지정한 파일을 제거합니다.")
    @DeleteMapping("/{id}")
    public ApiResponse<List<Object>> deleteData(@PathVariable("id") Long fileId) {
        return ApiResponse.okMessage("파일이 성공적으로 삭제되었습니다.");
    }

    @Operation(
            summary = "파일 공유 링크 생성",
            description = """
    지정된 파일에 대해 공유 링크를 생성합니다.  
    - 기본 만료 시간은 24시간입니다.  
    - 이미 공유 중인 파일은 기존 링크를 재사용합니다.  
    - 로그인된 사용자만 호출할 수 있습니다.
    """
    )
    @PostMapping("/{fileId}/share")
    public ApiResponse<FileShareResponse> createShareLink(
            @AuthenticationPrincipal Users user,
            @Parameter(description = "파일 ID", example = "15") @PathVariable Long fileId,
            @RequestBody(required = false) FileShareRequest request
    ) {
        FileShareResponse response = dataService.createShareLink(
                user.getId(),
                fileId,
                request != null ? request : new FileShareRequest(24) // 기본 24시간
        );
        return ApiResponse.ok(response, "공유 링크가 생성되었습니다.");
    }

    @Operation(
            summary = "파일 목록(타임라인) 조회",
            description = """
        로그인한 사용자의 파일 목록을 페이징, 정렬, 필터 조건으로 조회합니다.  
        기본적으로 업로드 최신순(`createdAt DESC`)으로 정렬되어 **타임라인 형태**로 반환됩니다.  
        
        **옵션**
        - `filterType`: category | favorite | tag | null(null을 보내면 전체파일을 조회)
        - `filterId`: categoryId (filterType이 'category'거나'tag'일때 해당 카테고리나 태그의 id입력)
        - `sortBy`: 정렬 기준 필드명 (예: createdAt, views ,file_size, last_viewed_at등)
        - `direction`: 정렬 방향 (ASC / DESC)
        - `page`, `size`: 페이징 설정 (기본 0페이지, size 최대 50)
        
        **예시**
        ```json
        {
          "page": 0,
          "size": 50,
          "sortBy": "createdAt",
          "direction": "DESC"
        }
        ```
        """
    )

    @PostMapping("/list")
    public ApiResponse<PagedResponseDto<DataResponse>> getFileList(
            @AuthenticationPrincipal Users user,
            @RequestBody PageRequestDto request
    ) {
        Page<DataResponse> filePage = dataService.getFiles(user, request);

        PagedResponseDto<DataResponse> response = PagedResponseDto.<DataResponse>builder()
                .content(filePage.getContent())
                .page(filePage.getNumber())
                .size(filePage.getSize())
                .totalElements(filePage.getTotalElements())
                .totalPages(filePage.getTotalPages())
                .last(filePage.isLast())
                .build();

        return ApiResponse.ok(response, "파일 목록 조회 성공");
    }

    @Operation(
            summary = "파일 내용(context) 수정",
            description = """
        파일의 텍스트 설명(context)만 수정합니다.  
        다른 필드는 변경되지 않습니다.
        """
    )
    @PutMapping("/{fileId}")
    public ApiResponse<FileDetailResponse> updateFileContext(
            @AuthenticationPrincipal Users user,
            @Parameter(description = "파일 ID", example = "15") @PathVariable Long fileId,
            @RequestBody FileUpdateRequest request
    ) {
        FileDetailResponse updated = dataService.updateFileContext(user, fileId, request);
        return ApiResponse.ok(updated, "파일 설명이 수정되었습니다.");
    }

    @Operation(
            summary = "파일 후처리 (OCR + 임베딩 + 카테고리 분석)",
            description = "S3에 업로드된 파일을 분석하여 OCR, 임베딩, 카테고리 및 태그 정보를 자동으로 갱신합니다."
    )
    @PostMapping("/{fileId}/post-process")
    public ApiResponse<File> postProcessingFile(
            @AuthenticationPrincipal Users user,
            @PathVariable Long fileId
    ) throws Exception {
        File updated = dataService.postProcessingFile(user, fileId);
        return ApiResponse.ok(updated, "파일 후처리가 완료되었습니다.");
    }
}

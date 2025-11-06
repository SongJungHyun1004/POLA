package com.jinjinjara.pola.data.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.common.dto.FileResponseDto;
import com.jinjinjara.pola.common.dto.PageRequestDto;
import com.jinjinjara.pola.common.dto.PagedResponseDto;
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
            @Parameter(description = "새 카테고리 이름", example = "디자인") @RequestParam String categoryName
    ) {
        File updated = dataService.updateFileCategory(fileId, categoryName, user);
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

    // ==========================================
    // 홈/카테고리 조회 (샘플)
    // ==========================================
    @Operation(summary = "카테고리별 데이터 조회",
            description = "유저 프로필과 함께 홈 화면에서 사용될 파일들을 리턴합니다.\n" +
                    "즐겨찾기, 리마인드, 최신 업로드 등 데이터를 포함합니다.")
    @GetMapping("/home")
    public ApiResponse<HomeDataResponse> getRecentDataList() {

        UserInfoResponse user = UserInfoResponse.builder()
                .id(1L)
                .email("user@google.com")
                .displayName("PolaUser")
                .profileImageUrl("https://s3-bucket/path/to/profile.png")
                .createdAt(LocalDateTime.parse("2025-10-27T10:00:00"))
                .build();

        List<DataResponse> favorites = List.of(
                new DataResponse(101L, "https://s3-bucket/path/to/image1.png", "image/png", "파란색 버튼이 있는 로그인 화면", true),
                new DataResponse(102L, "https://s3-bucket/path/to/image2.png", "image/jpeg", "회원가입 화면 버튼 디자인", true)
        );

        List<DataResponse> remind = List.of(
                new DataResponse(201L, "https://s3-bucket/path/to/remind1.png", "image/png", "UI 개선 피드백 이미지", false)
        );

        List<DataResponse> recent = List.of(
                new DataResponse(301L, "https://s3-bucket/path/to/recent1.png", "image/png", "다크 모드 메인 페이지 시안", true),
                new DataResponse(302L, "https://s3-bucket/path/to/recent2.png", "image/png", "프로필 편집 페이지 시안", false)
        );

        List<CategoryDataResponse> categories = List.of(
                CategoryDataResponse.builder()
                        .id(5L)
                        .categoryName("UI/UX")
                        .categorySort(1)
                        .data(List.of(
                                new DataResponse(401L, "https://s3-bucket/path/to/uiux1.png", "image/png", "버튼 컬러 실험 디자인", false),
                                new DataResponse(402L, "https://s3-bucket/path/to/uiux2.png", "image/png", "로그인 입력창 배치 시안", true)
                        ))
                        .build()
        );

        HomeDataResponse data = HomeDataResponse.builder()
                .userInfo(user)
                .favoriteData(favorites)
                .remindData(remind)
                .recentData(recent)
                .categoryData(categories)
                .build();

        return ApiResponse.ok(data, "데이터 목록 조회에 성공했습니다.");
    }
    @Operation(
            summary = "파일 목록(타임라인) 조회",
            description = """
        로그인한 사용자의 파일 목록을 페이징, 정렬, 필터 조건으로 조회합니다.  
        기본적으로 업로드 최신순(`createdAt DESC`)으로 정렬되어 **타임라인 형태**로 반환됩니다.  
        
        **옵션**
        - `filterType`: category | favorite | (없음)
        - `filterId`: categoryId (filterType이 'category'일 때만 필요)
        - `sortBy`: 정렬 기준 필드명 (예: createdAt, views 등)
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
    public ApiResponse<PagedResponseDto<FileResponseDto>> getFileList(
            @AuthenticationPrincipal Users user,
            @RequestBody PageRequestDto request
    ) {
        Page<File> filePage = dataService.getFiles(user, request);

        List<FileResponseDto> fileResponses = filePage.getContent().stream()
                .map(FileResponseDto::fromEntity)
                .toList();

        PagedResponseDto<FileResponseDto> response = PagedResponseDto.<FileResponseDto>builder()
                .content(fileResponses)
                .page(filePage.getNumber())
                .size(filePage.getSize())
                .totalElements(filePage.getTotalElements())
                .totalPages(filePage.getTotalPages())
                .last(filePage.isLast())
                .build();

        return ApiResponse.ok(response, "파일 목록 조회 성공");
    }


}

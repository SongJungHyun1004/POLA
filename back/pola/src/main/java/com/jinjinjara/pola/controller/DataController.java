package com.jinjinjara.pola.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.data.dto.common.Platform;
import com.jinjinjara.pola.data.dto.request.FileUploadCompleteRequest;
import com.jinjinjara.pola.data.dto.response.*;
import com.jinjinjara.pola.data.entity.FileEntity;
import com.jinjinjara.pola.data.service.DataService;
import com.jinjinjara.pola.user.dto.response.UserInfoResponse;
import com.jinjinjara.pola.user.entity.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "Data API", description = "데이터 API")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class DataController {

    private final DataService dataService;

    @Operation(
            summary = "파일 업로드 완료 처리",
            description = "클라이언트에서 Presigned URL로 S3 업로드가 끝난 후, 해당 파일 메타데이터를 DB에 저장합니다."
    )
    @PostMapping("/complete")
    public ApiResponse<FileEntity> saveUploadedFile(
            @AuthenticationPrincipal Users user,
            @RequestBody FileUploadCompleteRequest request
    ) {
        if (user == null) {
            throw new RuntimeException("인증 정보가 유효하지 않습니다.");
            // TODO 나중에 에러코드 변환
        }

        FileEntity savedFile = dataService.saveUploadedFile(user, request);
        return ApiResponse.ok(savedFile, "파일이 성공적으로 등록되었습니다.");
    }




    @Operation(summary = "데이터 삭제", description = "사용자가 지정한 파일을 제거합니다.")
    @DeleteMapping("/{id}")
    public ApiResponse<List<Object>> deleteData(
            @PathVariable("id") Long fileId
    ) {
        return ApiResponse.okMessage("파일이 성공적으로 삭제되었습니다.");
    }


    @Operation(summary = "카테고리별 데이터 조회",
            description =   "유저 프로필과 함께 홈 화면에서 사용될 파일들을 리턴합니다.\n" +
                            "즐겨찾기, 리마인드 최신 순 3장\n" +
                            "최근 저장된 파일 10장\n" +
                            "카테고리 별 데이터 최신 순 5장")
    @GetMapping("/categories/{id}")
    public ApiResponse<List<DataResponse>> getCategoryDataList(
            @PathVariable("id") Long categoryId
    ) {
        List<DataResponse> data = new ArrayList<>();
        data.add(new DataResponse(101L, "https://s3-bucket/path/to/image1.png", "image/png", "파란색 버튼이 있는 로그인 화면", true));
        data.add(new DataResponse(102L, "https://s3-bucket/path/to/image2.png", "image/png", "회원가입 폼 디자인", false));
        return ApiResponse.ok(data, "데이터 목록 조회에 성공했습니다.");
    }

    @Operation(
            summary = "카테고리별 데이터 조회",
            description = "사용자가 선택한 카테고리의 파일들을 최근 날짜순으로 리턴합니다."
    )
    @GetMapping("/home")
    public ApiResponse<HomeDataResponse> getRecentDataList() {

        // 유저 정보
        UserInfoResponse user = UserInfoResponse.builder()
                .id(1L)
                .email("user@google.com")
                .displayName("PolaUser")
                .profileImageUrl("https://s3-bucket/path/to/profile.png")
                .createdAt(LocalDateTime.parse("2025-10-27T10:00:00"))
                .build();

        // 즐겨찾기 데이터 3장
        List<DataResponse> favorites = List.of(
                new DataResponse(101L, "https://s3-bucket/path/to/image1.png", "image/png", "파란색 버튼이 있는 로그인 화면", true),
                new DataResponse(102L, "https://s3-bucket/path/to/image2.png", "image/jpeg", "회원가입 화면 버튼 디자인", true)
        );

        // 리마인드 데이터 3장
        List<DataResponse> remind = List.of(
                new DataResponse(201L, "https://s3-bucket/path/to/remind1.png", "image/png", "UI 개선 피드백 이미지", false)
        );

        // 최근 업로드 데이터 10장
        List<DataResponse> recent = List.of(
                new DataResponse(301L, "https://s3-bucket/path/to/recent1.png", "image/png", "다크 모드 메인 페이지 시안", true),
                new DataResponse(302L, "https://s3-bucket/path/to/recent2.png", "image/png", "프로필 편집 페이지 시안", false)
        );

        // 카테고리별 데이터 5개
        List<CategoryDataResponse> categories = List.of(
                CategoryDataResponse.builder()
                        .id(5L)
                        .categoryName("UI/UX")
                        .categorySort(1)
                        .data(List.of(
                                new DataResponse(401L, "https://s3-bucket/path/to/uiux1.png", "image/png", "버튼 컬러 실험 디자인", false),
                                new DataResponse(402L, "https://s3-bucket/path/to/uiux2.png", "image/png", "로그인 입력창 배치 시안", true)
                        ))
                        .build(),
                CategoryDataResponse.builder()
                        .id(6L)
                        .categoryName("Frontend")
                        .categorySort(2)
                        .data(List.of(
                                new DataResponse(501L, "https://s3-bucket/path/to/frontend1.png", "image/png", "컴포넌트 구조 다이어그램", true),
                                new DataResponse(502L, "https://s3-bucket/path/to/frontend2.png", "image/png", "상태관리 로직 플로우", false)
                        ))
                        .build(),
                CategoryDataResponse.builder()
                        .id(7L)
                        .categoryName("Backend")
                        .categorySort(3)
                        .data(List.of(
                                new DataResponse(601L, "https://s3-bucket/path/to/backend1.png", "image/png", "API 응답 구조 예시", true)
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
}

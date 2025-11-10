package com.jinjinjara.pola.share.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.share.dto.response.ShareFileResponse;
import com.jinjinjara.pola.share.service.ShareService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @Operation(summary = "공유된 파일 정보 조회", description = "공유 토큰을 이용해 presigned URL 및 파일 정보를 조회합니다.")
    @GetMapping("/{token}")
    public ApiResponse<ShareFileResponse> getSharedFileInfo(@PathVariable String token) {
        ShareFileResponse response = shareService.getSharedFileInfo(token);
        return ApiResponse.ok(response, "공유 파일 조회 성공");
    }
}

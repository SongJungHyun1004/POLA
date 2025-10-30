package com.jinjinjara.pola.controller;

import com.jinjinjara.pola.common.ControllerHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "User API", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController implements ControllerHelper {

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo() {
        return handleSuccess(1, HttpStatus.OK, "유저 정보를 가져왔습니다.");
    }
}

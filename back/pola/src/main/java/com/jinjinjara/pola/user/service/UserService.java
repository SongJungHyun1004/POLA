package com.jinjinjara.pola.user.service;

import com.jinjinjara.pola.user.dto.response.UserInfoResponse;
import com.jinjinjara.pola.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    /**
     * 현재 로그인된 사용자 엔티티를 DTO로 변환합니다.
     * @param user @AuthenticationPrincipal을 통해 SecurityContext에서 직접 가져온 Users 객체
     * @return 사용자 정보 DTO
     */
    public UserInfoResponse getCurrentUserInfo(Users user) {
        // 컨트롤러에서 @AuthenticationPrincipal을 통해 이미 완전한 Users 객체를 받았으므로,
        // 별도의 DB 조회 없이 바로 DTO로 변환합니다.
        return new UserInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getProfileImageUrl(),
                user.getCreatedAt()
        );
    }
}

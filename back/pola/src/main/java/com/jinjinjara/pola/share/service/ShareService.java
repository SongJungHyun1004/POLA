package com.jinjinjara.pola.share.service;

import com.jinjinjara.pola.auth.repository.UserRepository;
import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.entity.FileTag;
import com.jinjinjara.pola.data.repository.FileRepository;
import com.jinjinjara.pola.data.repository.FileTagRepository;
import com.jinjinjara.pola.s3.service.S3Service;
import com.jinjinjara.pola.share.dto.response.ShareFileResponse;
import com.jinjinjara.pola.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShareService {

    private final FileRepository fileRepository;
    private final FileTagRepository fileTagRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    /**
     * 🔹 공유 토큰으로 파일 정보 조회 + Presigned URL 생성 (미리보기 + 다운로드)
     */
    public ShareFileResponse getSharedFileInfo(String token) {
        File file = fileRepository.findByShareToken(token)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        // 공유 만료 검사
        if (file.getShareExpiredAt() != null && file.getShareExpiredAt().isBefore(LocalDateTime.now())) {
            throw new CustomException(ErrorCode.SHARE_EXPIRED, "공유 링크가 만료되었습니다.");
        }

        // 공유 비활성화 상태일 경우
        if (Boolean.FALSE.equals(file.getShareStatus())) {
            throw new CustomException(ErrorCode.SHARE_DISABLED, "이 파일은 더 이상 공유되지 않습니다.");
        }

        // Presigned URL 생성
        String previewUrl = s3Service.generateGetUrl(file.getSrc(), false);
        String downloadUrl = s3Service.generateGetUrl(file.getSrc(), true);

        // 태그 조회
        List<String> tags = fileTagRepository.findByFile(file).stream()
                .map(ft -> ft.getTag().getTagName())
                .collect(Collectors.toList());

        // 파일 소유자 이름 조회
        String ownerName = userRepository.findById(file.getUserId())
                .map(Users::getDisplayName)
                .orElse("Unknown");

        // 응답 DTO 구성
        return ShareFileResponse.builder()
                .fileId(file.getId())
                .presignedUrl(previewUrl)
                .downloadUrl(downloadUrl)
                .type(file.getType())
                .context(file.getContext())
                .ocrText(file.getOcrText())
                .fileSize(file.getFileSize())
                .platform(file.getPlatform())
                .originUrl(file.getOriginUrl())
                .createdAt(file.getCreatedAt())
                .ownerName(ownerName)
                .tags(tags)
                .build();
    }
}

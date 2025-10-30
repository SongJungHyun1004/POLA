package com.jinjinjara.pola.s3;


import com.jinjinjara.pola.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files") // API의 기본 경로를 /api/files로 설정
public class S3Controller {

    private final S3Service s3Service;

    /**
     * S3에 파일을 업로드합니다.
     * 클라이언트는 'file'이라는 Key로 파일 데이터를, 'directory'라는 Key로 디렉토리 이름을
     * 함께 POST 요청으로 보내야 합니다.
     *
     * @param file      업로드할 파일 (multipart/form-data)
     * @param directory S3 버킷 내의 저장할 디렉토리 이름 (예: "home", "profile", "posts")
     * @return 업로드된 파일의 S3 URL
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam("directory") String directory
    ) {
        String fileUrl = s3Service.uploadFile(file, directory);
        return ResponseEntity.ok(fileUrl);
    }
}
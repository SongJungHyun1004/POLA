package com.jinjinjara.pola.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    /* ------------------------- [인증 관련] ------------------------- */
    AUTHENTICATION_FAILED("AUTH-001", "인증에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    INVALID_ID_TOKEN("AUTH-002", "유효하지 않은 ID 토큰입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_ACCESS_TOKEN("AUTH-003", "유효하지 않은 엑세스 토큰입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN("AUTH-004", "유효하지 않은 리프레시 토큰입니다.", HttpStatus.UNAUTHORIZED),

    /* ------------------------- [유저 관련] ------------------------- */
    USER_NOT_FOUND("USER-001", "유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_UNAUTHORIZED("USER-002", "인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED),
    USER_FORBIDDEN("USER-003", "해당 작업을 수행할 권한이 없습니다.", HttpStatus.FORBIDDEN),
    USER_ALREADY_EXISTS("USER-004", "이미 가입된 유저입니다.", HttpStatus.CONFLICT),

    /* ------------------------- [카테고리 관련] ------------------------- */
    CATEGORY_NOT_FOUND("CATEGORY-001", "카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CATEGORY_CREATE_FAIL("CATEGORY-002", "카테고리 생성에 실패했습니다.", HttpStatus.BAD_REQUEST),
    CATEGORY_UPDATE_FAIL("CATEGORY-003", "카테고리 수정에 실패했습니다.", HttpStatus.BAD_REQUEST),
    CATEGORY_DELETE_FAIL("CATEGORY-004", "카테고리 삭제에 실패했습니다.", HttpStatus.BAD_REQUEST),
    CATEGORY_ALREADY_EXISTS("CATEGORY-005", "이미 존재하는 카테고리 이름입니다.", HttpStatus.CONFLICT),
    CATEGORY_LIST_EMPTY("CATEGORY-006", "등록된 카테고리가 없습니다.", HttpStatus.NOT_FOUND),
    CATEGORY_ACCESS_DENIED("CATEGORY-007", "해당 카테고리에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),

    /* ------------------------- [태그 관련] ------------------------- */
    TAG_NOT_FOUND("TAG-001", "태그를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    TAG_ALREADY_EXISTS("TAG-002", "이미 존재하는 태그입니다.", HttpStatus.CONFLICT),
    TAG_LINK_DUPLICATE("TAG-003", "이미 연결된 태그입니다.", HttpStatus.CONFLICT),
    TAG_CREATE_FAIL("TAG-004", "태그 생성에 실패했습니다.", HttpStatus.BAD_REQUEST),

    /* ------------------------- [파일 관련] ------------------------- */
    FILE_NOT_FOUND("FILE-001", "파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    FILE_UPLOAD_FAIL("FILE-002", "파일 업로드 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_DELETE_FAIL("FILE-003", "파일 삭제에 실패했습니다.", HttpStatus.BAD_REQUEST),
    FILE_CATEGORY_UPDATE_FAIL("FILE-004", "파일 카테고리 변경에 실패했습니다.", HttpStatus.BAD_REQUEST),
    FILE_ACCESS_DENIED("FILE-005", "해당 파일에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),

    /* ------------------------- [데이터 처리 / 일반] ------------------------- */
    DATA_NOT_FOUND("DATA-001", "데이터를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DATA_SAVE_FAIL("DATA-002", "데이터 저장 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    DATA_DELETE_FAIL("DATA-003", "데이터 삭제 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    /* ------------------------- [검색 / OpenSearch 관련] ------------------------- */
    SEARCH_FAIL("SEARCH-001", "검색 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    SEARCH_INDEX_FAIL("SEARCH-002", "검색 인덱스 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    SEARCH_UPDATE_FAIL("SEARCH-003", "검색 문서 업데이트에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    /* ------------------------- [유효성 / 요청 관련] ------------------------- */
    INVALID_REQUEST("COMMON-001", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR("COMMON-002", "요청 데이터가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),

    /* ------------------------- [서버 공통 / 내부 오류] ------------------------- */
    INTERNAL_SERVER_ERROR("SERVER-001", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    SHARE_EXPIRED("SHARE-001", "공유 링크가 만료되었습니다.", HttpStatus.GONE),
    SHARE_DISABLED("SHARE-002", "이 파일은 더 이상 공유되지 않습니다.", HttpStatus.FORBIDDEN),
    SHARE_NOT_FOUND("SHARE-003", "유효하지 않은 공유 링크입니다.", HttpStatus.NOT_FOUND);
    private final String code;
    private final String message;
    private final HttpStatus status;
}

package gon.til.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode {

    // 유저 오류코드
    DUPLICATE_USER_NAME(HttpStatus.CONFLICT, 409, "이미 존재하는 사용자명입니다."),
    DUPLICATE_USER_EMAIL(HttpStatus.CONFLICT, 409, "이미 존재하는 이메일입니다."),

    NOT_FOUND_USER_EMAIL(HttpStatus.NOT_FOUND, 404, "이메일이 존재하지 않습니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, 404, "유저가 존재하지 않습니다."),

    // 프로젝트 오류코드
    DUPLICATE_PROJECT_TITLE(HttpStatus.CONFLICT, 409, "같은 이름의 프로젝트가 존재합니다."),
    NOT_FOUND_PROJECT(HttpStatus.NOT_FOUND, 404, "프로젝트가 존재하지 않습니다."),
    ACCESS_DENIED_PROJECT(HttpStatus.FORBIDDEN, 403, "프로젝트 접근 권한이 없습니다."),

    // 보드 오류코드
    DUPLICATE_BOARD(HttpStatus.CONFLICT, 409, "이미 보드가 존재합니다."),
    NOT_FOUND_BOARD(HttpStatus.NOT_FOUND, 404, "보드가 존재하지 않습니다."),
    ACCESS_DENIED_BOARD(HttpStatus.FORBIDDEN, 403, "보드 접근 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;
}

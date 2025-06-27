package gon.til.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GlobalException extends RuntimeException {

    private final GlobalErrorCode globalErrorCode;

    public GlobalException(GlobalErrorCode globalErrorCode) {
        super(globalErrorCode.getMessage());
        this.globalErrorCode = globalErrorCode;
    }

    public HttpStatus getStatus() {
        return globalErrorCode.getHttpStatus();
    }

    public GlobalErrorCode getGlobalErrorCode() {
        return globalErrorCode.getCode();
    }
}

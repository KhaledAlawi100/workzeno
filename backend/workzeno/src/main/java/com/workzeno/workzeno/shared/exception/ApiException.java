package com.workzeno.workzeno.shared.exception;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String title;

    public ApiException(
            HttpStatus status,
            String title,
            String detail
    ) {
        super(detail);
        this.status = status;
        this.title = title;
    }

}

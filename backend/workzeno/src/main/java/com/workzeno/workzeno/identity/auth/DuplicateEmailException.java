package com.workzeno.workzeno.identity.auth;

import com.workzeno.workzeno.shared.exception.ApiException;
import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends ApiException {

    public DuplicateEmailException() {
        super(
                HttpStatus.CONFLICT,
                "Registration conflict",
                "An account with this email already exists"
        );
    }
}

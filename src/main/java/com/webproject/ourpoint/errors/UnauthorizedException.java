package com.webproject.ourpoint.errors;

import com.webproject.ourpoint.utils.MessageUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "Unauthorized")
public class UnauthorizedException extends ServiceRuntimeException {

    public static final String MESSAGE_KEY = "error.auth";

    public static final String MESSAGE_DETAIL = "error.auth.details";

    public UnauthorizedException(String message) {
        super(MESSAGE_KEY, MESSAGE_DETAIL, new Object[]{message});
    }

    @Override
    public String getMessage() {
        return MessageUtils.getMessage(getDetailKey(), getParams());
    }

    @Override
    public String toString() {
        return MessageUtils.getMessage(getMessageKey());
    }

}

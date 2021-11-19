package com.webproject.ourpoint.errors;

import com.webproject.ourpoint.utils.MessageUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Data Not Found")
public class NotFoundException extends ServiceRuntimeException {

    public static final String MESSAGE_KEY = "error.notfound";

    public static final String MESSAGE_DETAIL = "error.notfound.details";

    public NotFoundException(String message) {
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

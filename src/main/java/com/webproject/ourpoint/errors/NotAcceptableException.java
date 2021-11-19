package com.webproject.ourpoint.errors;

import com.webproject.ourpoint.utils.MessageUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE, reason = "Not Allowed")
public class NotAcceptableException extends ServiceRuntimeException {

  public static final String MESSAGE_KEY = "error.validation";

  public static final String MESSAGE_DETAIL = "error.validation.details";

  public NotAcceptableException(String message) {
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
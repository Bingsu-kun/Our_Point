package com.webproject.flarepoint.security;

import com.webproject.flarepoint.model.user.User;
import lombok.Getter;
import lombok.ToString;

import static com.google.common.base.Preconditions.checkArgument;

@ToString
@Getter
public class AuthenticationResult {

  private final String apiToken;

  private final User user;

  public AuthenticationResult(String apiToken, User user) {
    checkArgument(apiToken != null, "apiToken must be provided.");
    checkArgument(user != null, "user must be provided.");

    this.apiToken = apiToken;
    this.user = user;
  }

}
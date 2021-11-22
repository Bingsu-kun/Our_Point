package com.webproject.flarepoint.security;

import com.webproject.flarepoint.model.user.Fisher;
import lombok.Getter;
import lombok.ToString;

import static com.google.common.base.Preconditions.checkArgument;

@ToString
@Getter
public class AuthenticationResult {

  private final String apiToken;

  private final Fisher fisher;

  public AuthenticationResult(String apiToken, Fisher fisher) {
    checkArgument(apiToken != null, "apiToken must be provided.");
    checkArgument(fisher != null, "user must be provided.");

    this.apiToken = apiToken;
    this.fisher = fisher;
  }

}
package com.webproject.ourpoint.security;

import com.webproject.ourpoint.model.user.Fisher;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import static com.google.common.base.Preconditions.checkArgument;

public class AuthenticationResult {

  private final String apiToken;

  private final Fisher fisher;

  public AuthenticationResult(String apiToken, Fisher fisher) {
    checkArgument(apiToken != null, "apiToken must be provided.");
    checkArgument(fisher != null, "user must be provided.");

    this.apiToken = apiToken;
    this.fisher = fisher;
  }

  public String getApiToken() {
    return apiToken;
  }

  public Fisher getUser() {
    return fisher;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
      .append("apiToken", apiToken)
      .append("user", fisher)
      .toString();
  }

}
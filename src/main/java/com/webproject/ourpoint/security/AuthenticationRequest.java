package com.webproject.ourpoint.security;

import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@ToString
public class AuthenticationRequest {

  private String principal;

  private String credentials;

  protected AuthenticationRequest() {}

  public AuthenticationRequest(String principal, String credentials) {
    this.principal = principal;
    this.credentials = credentials;
  }

}
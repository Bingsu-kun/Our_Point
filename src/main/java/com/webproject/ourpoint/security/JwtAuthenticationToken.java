package com.webproject.ourpoint.security;

import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;


@ToString
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

  /*
   * 로그인 전에는 String 타입이고, 로그인 후에는 JwtAuthentication 타입이다.
   * 컨트롤러에서 AuthenticationPrincipal 어노테이션을 사용하면 쉽게 접근할 수 있다.
   */
  private final Object principal;

  private String credentials;

  public JwtAuthenticationToken(String principal, String credentials) {
    super(null);
    super.setAuthenticated(false);

    this.principal = principal;
    this.credentials = credentials;
  }

  JwtAuthenticationToken(Object principal, String credentials, Collection<? extends GrantedAuthority> authorities) {
    super(authorities);
    super.setAuthenticated(true);

    this.principal = principal;
    this.credentials = credentials;
  }

  AuthenticationRequest authenticationRequest() {
    return new AuthenticationRequest(String.valueOf(principal), credentials);
  }

  @Override
  public Object getPrincipal() {
    return principal;
  }

  @Override
  public String getCredentials() {
    return credentials;
  }

  public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    if (isAuthenticated) {
      throw new IllegalArgumentException("Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
    }
    super.setAuthenticated(false);
  }

  @Override
  public void eraseCredentials() {
    super.eraseCredentials();
    credentials = null;
  }

}
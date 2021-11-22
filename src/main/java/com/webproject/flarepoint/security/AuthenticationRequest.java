package com.webproject.flarepoint.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {

  private String principal;

  private String credentials;

}
package com.webproject.ourpoint.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {

  private String principal;

  private String credentials;

}
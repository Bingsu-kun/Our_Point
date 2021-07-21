package com.webproject.ourpoint.security;

import com.webproject.ourpoint.errors.NotFoundException;
import com.webproject.ourpoint.model.user.Fisher;
import com.webproject.ourpoint.model.user.Role;
import com.webproject.ourpoint.service.FisherService;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.apache.commons.lang3.ClassUtils.isAssignable;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;


public class JwtAuthenticationProvider implements AuthenticationProvider {

  private final Jwt jwt;

  private final FisherService fisherService;

  public JwtAuthenticationProvider(Jwt jwt, FisherService fisherService) {
    this.jwt = jwt;
    this.fisherService = fisherService;
  }

  // true를 리턴하면 Provider에서 인증처리 가능함을 의미. JwtAuthenticationToken 타입처리 가능
  @Override
  public boolean supports(Class<?> authentication) {
    return isAssignable(JwtAuthenticationToken.class, authentication);
  }

  // 반환 값이 null이 아니면 인증 처리 완료됨.
  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) authentication;
    return processUserAuthentication(authenticationToken.authenticationRequest());
  }

  private Authentication processUserAuthentication(AuthenticationRequest request) {
    try {
      Fisher fisher = fisherService.login(request.getPrincipal(), request.getCredentials());
      JwtAuthenticationToken authenticated =
        // 응답용 Authentication 인스턴스를 생성한다.
        // JwtAuthenticationToken.principal 부분에는 JwtAuthentication 인스턴스가 set 된다.
        new JwtAuthenticationToken(new JwtAuthentication(fisher.getId(), fisher.getUsername(), fisher.getEmail()), null, createAuthorityList(fisher.getRole()));
      // JWT 값을 생성한다.
      String apiToken = fisher.newApiToken(jwt, new String[]{fisher.getRole()});
      authenticated.setDetails(new AuthenticationResult(apiToken, fisher));
      return authenticated;
    } catch (NotFoundException e) {
      throw new UsernameNotFoundException(e.getMessage());
    } catch (IllegalArgumentException e) {
      throw new BadCredentialsException(e.getMessage());
    } catch (DataAccessException e) {
      throw new AuthenticationServiceException(e.getMessage(), e);
    }
  }

}
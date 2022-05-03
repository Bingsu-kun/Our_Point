package com.webproject.flarepoint.security;

import com.webproject.flarepoint.errors.NotFoundException;
import com.webproject.flarepoint.model.user.User;
import com.webproject.flarepoint.service.UserService;
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

  private final UserService userService;

  public JwtAuthenticationProvider(Jwt jwt, UserService userService) {
    this.jwt = jwt;
    this.userService = userService;
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
      User user = userService.login(request.getPrincipal(), request.getCredentials());
      JwtAuthenticationToken authenticated =
        // 응답용 Authentication 인스턴스를 생성한다.
        // JwtAuthenticationToken.principal 부분에는 JwtAuthentication 인스턴스가 set 된다.
        new JwtAuthenticationToken(new JwtAuthentication(user.getId(), user.getUserName(), user.getEmail()), null, createAuthorityList(user.getRole()));
      // JWT 값을 생성한다.
      String apiToken = user.newApiToken(jwt, new String[]{user.getRole()});
      authenticated.setDetails(new AuthenticationResult(apiToken, user));
      String refreshToken = user.newRefreshToken(jwt, new String[]{user.getRole()});
      authenticated.setDetails(new AuthenticationResult(refreshToken, user));
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
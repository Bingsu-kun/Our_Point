package com.webproject.ourpoint.security;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.webproject.ourpoint.service.FisherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static com.webproject.ourpoint.utils.CookieUtil.createCookie;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static com.webproject.ourpoint.utils.CookieUtil.getTokenCookie;

/*
 * HTTP Request-Header 에서 JWT 값을 추출하고, JWT 값이 올바르다면 인증정보 JwtAuthenticationToken을 생성한다.
 * 생성된 인스턴스는 SecurityContextHolder} 통해 Thread-Local 영역에 저장된다.
 */
public class JwtAuthenticationTokenFilter extends GenericFilterBean {

  private static final Pattern BEARER = Pattern.compile("^Bearer$", Pattern.CASE_INSENSITIVE);

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final String headerKey;

  private final Jwt jwt;

  @Autowired
  private FisherService fisherService;

  public JwtAuthenticationTokenFilter(String headerKey, Jwt jwt) {
    this.headerKey = headerKey;
    this.jwt = jwt;
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
    throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    // SecurityContextHolder 에서 인증정보를 찾을 수 없다면...
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      // HTTP 요청 Header 에서 AccessToken 값을, Cookie에서 RefreshToken 값을 가져와본다.
      String authorizationToken = obtainAuthorizationToken(request);
      String refreshToken = null;
      // AccessToken 값이 있다면, AccessToken 값을 검증하고 인증정보를 생성해 SecurityContextHolder 에 추가한다.
      if (authorizationToken != null) {
        try {
          Jwt.Claims AccessClaims = verify(authorizationToken);
          log.debug("Jwt parse result: {}", AccessClaims);

          // 만료가 아직 안됐다면 바로 리프레쉬해서 response에 set. false값을 리턴한다면 만료된 상황.
          if (canRefresh(AccessClaims)) {
            String refreshedAccessToken = jwt.refreshAccessToken(authorizationToken);
            response.setHeader(headerKey, refreshedAccessToken);
          }
          // AccessToken이 만료이니 refreshToken을 가져온다.
          else {
            Cookie refreshCookie = getTokenCookie(request , Jwt.REFRESH_TOKEN_NAME);
            if (refreshCookie != null) {
              refreshToken = refreshCookie.getValue();
            }
          }

          Long userKey = AccessClaims.userKey;
          String name = AccessClaims.name;
          String email = AccessClaims.email;

          List<GrantedAuthority> authorities = obtainAuthorities(AccessClaims);

          if (nonNull(userKey) && isNotEmpty(name) && isNotEmpty(email) && authorities.size() > 0) {
            JwtAuthenticationToken authentication =
                    new JwtAuthenticationToken(new JwtAuthentication(userKey, name, email), null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
          }
        }
        catch (Exception e) {
          log.warn("AccessToken processing failed: {}", e.getMessage());
        }
        try {
          if(refreshToken != null) {
            Jwt.Claims RefreshClaims = verify(refreshToken);
            log.debug("Jwt parse result: {}", RefreshClaims);

            // 만료가 아직 안됐다면 바로 리프레쉬해서 response에 add. false값을 리턴한다면 만료된 상황.
            if (canRefresh(RefreshClaims)) {
              String refreshedRefreshToken = jwt.refreshRefreshToken(refreshToken);
              Cookie refreshedRefreshCookie = createCookie(Jwt.REFRESH_TOKEN_NAME, refreshedRefreshToken);
              response.addCookie(refreshedRefreshCookie);
            }

            //TODO - Redis 연동해서 refreshToken과 같은 값 찾아온 후 인증 절차 처리.

            Long userKey = RefreshClaims.userKey;
            String name = RefreshClaims.name;
            String email = RefreshClaims.email;

            /*
            redisToken 변수 생성

            RuserKey, Rname, Remail 생성.

             if ( 키, 네임, 이메일 매칭) {
                JwtAuthenticationToken authentication =
                    new JwtAuthenticationToken(new JwtAuthentication(userKey, name, email), null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                Fisher fisher = fisherService.findById(userKey);
                String newAccessToken = fisher.newApiToken(jwt, new String[]{fisher.getRole()});
                response.setHeader(headerKey, newAccessToken);
             }
             */
          }
        }
        catch (Exception e) {
          log.warn("RefreshToken processing failed: {}", e.getMessage());
        }
      }
    } else {
      log.debug("SecurityContextHolder not populated with security token, as it already contained: '{}'",
        SecurityContextHolder.getContext().getAuthentication());
    }

    chain.doFilter(request, response);
  }

  private boolean canRefresh(Jwt.Claims claims) {
    long exp = claims.exp();
    if (exp > 0) {
      long remain = exp - System.currentTimeMillis();
      return remain > (long) 0;
    }
    return false;
  }

  private List<GrantedAuthority> obtainAuthorities(Jwt.Claims claims) {
    String[] roles = claims.roles;
    return roles == null || roles.length == 0 ?
      Collections.emptyList() :
      Arrays.stream(roles).map(SimpleGrantedAuthority::new).collect(toList());
  }

  private String obtainAuthorizationToken(HttpServletRequest request) {
    String token = request.getHeader(headerKey);
    if (token != null) {
      if (log.isDebugEnabled())
        log.debug("Jwt authorization api detected: {}", token);
      token = URLDecoder.decode(token, StandardCharsets.UTF_8);
      String[] parts = token.split(" ");
      if (parts.length == 2) {
        String scheme = parts[0];
        String credentials = parts[1];
        return BEARER.matcher(scheme).matches() ? credentials : null;
      }
    }

    return null;
  }

  private Jwt.Claims verify(String token) {
    return jwt.verify(token);
  }

}
package com.webproject.ourpoint.security;

import com.webproject.ourpoint.errors.NotFoundException;
import com.webproject.ourpoint.model.user.Fisher;
import com.webproject.ourpoint.service.FisherService;
import com.webproject.ourpoint.utils.RedisUtil;
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
import static com.webproject.ourpoint.utils.CookieUtil.getTokenCookie;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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

  @Autowired
  private RedisUtil redisUtil;

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
      // HTTP 요청 Header 에서 AccessToken 값을, Cookie에서 RefreshCookie 값을 가져와본다.
      String AccessToken = obtainAuthorizationToken(request);
      Cookie refreshCookie = getTokenCookie(request , Jwt.REFRESH_TOKEN_NAME);
      String refreshToken = null;
      // AccessToken 값이 있다면, AccessToken 값을 검증하고 인증정보를 생성해 SecurityContextHolder 에 추가한다.
      // 만약 AccessToken 이 존재한다면
      if (AccessToken != null) {
        try {
          Jwt.Claims AccessClaims = verify(AccessToken);
          log.debug("AccessToken parse result: {}", AccessClaims);

          // 만료가 아직 안됐다면 바로 리프레쉬해서 response 에 set. false 값을 리턴한다면 만료된 상황.
          if (canRefresh(AccessClaims)) {
            String refreshedAccessToken = jwt.refreshAccessToken(AccessToken);
            response.setHeader(headerKey, refreshedAccessToken);

            setAuthByClaim(AccessClaims, request, response, false);
          }
          // 만료되었다면 refreshToken을 확인해서 재발급.
          else {
            if (refreshCookie != null) {
              try {
                refreshToken = refreshCookie.getValue();

                Jwt.Claims RefreshClaims = verify(refreshToken);
                log.debug("RefreshToken parse result: {}", RefreshClaims);

                // 만료가 아직 안됐다면 바로 리프레쉬해서 response 에 add. false 값을 리턴한다면 만료된 상황.
                if (canRefresh(RefreshClaims)) {
                  String refreshedRefreshToken = jwt.refreshRefreshToken(refreshToken);
                  Cookie refreshedRefreshCookie = createCookie(Jwt.REFRESH_TOKEN_NAME, refreshedRefreshToken);
                  refreshedRefreshCookie.setHttpOnly(true);
                  refreshedRefreshCookie.setSecure(true);
                  response.addCookie(refreshedRefreshCookie);

                  String redisToken = redisUtil.getData(refreshToken);
                  Jwt.Claims RedisClaims = verify(redisToken);
                  log.debug("RedisToken parse result: {}", RedisClaims);

                  setAuthByClaim(RedisClaims, request, response, true);
                }
              } catch (Exception e) {
                log.warn("RefreshToken processing failed: {}", e.getMessage());
              }
            }
          }
        }
        catch (Exception e) {
          log.warn("AccessToken processing failed: {}", e.getMessage());
        }
      }
      // AccessToken이 존재하지 않는다면
      else if (refreshCookie != null) {
        try {
          refreshToken = refreshCookie.getValue();

          Jwt.Claims RefreshClaims = verify(refreshToken);
          log.debug("RefreshToken parse result: {}", RefreshClaims);

          // 만료가 아직 안됐다면 바로 리프레쉬해서 response에 add. false값을 리턴한다면 만료된 상황.
          if (canRefresh(RefreshClaims)) {
            String refreshedRefreshToken = jwt.refreshRefreshToken(refreshToken);
            Cookie refreshedRefreshCookie = createCookie(Jwt.REFRESH_TOKEN_NAME, refreshedRefreshToken);
            refreshedRefreshCookie.setHttpOnly(true);
            refreshedRefreshCookie.setSecure(true);
            response.addCookie(refreshedRefreshCookie);

            String redisToken = redisUtil.getData(refreshToken);
            Jwt.Claims RedisClaims = verify(redisToken);
            log.debug("RedisToken parse result: {}", RedisClaims);

            setAuthByClaim(RedisClaims, request, response, true);
          }
        } catch (Exception e) {
          log.warn("RefreshToken processing failed: {}", e.getMessage());
        }
      }
      // 둘 다 존재하지 않음.
      else {
        log.debug("Access and Refresh Tokens not exists.");
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

  //isRefresh 파라미터는 이 함수가 쓰일때, RefreshToken 검증에 쓰이는지 확인하기 위함.
  private void setAuthByClaim(Jwt.Claims claims, HttpServletRequest request, HttpServletResponse response, boolean isRefresh) {
    Long userKey = claims.userKey;
    String name = claims.name;
    String email = claims.email;

    List<GrantedAuthority> authorities = obtainAuthorities(claims);

    if (nonNull(userKey) && isNotEmpty(name) && isNotEmpty(email) && authorities.size() > 0) {
      JwtAuthenticationToken authentication =
              new JwtAuthenticationToken(new JwtAuthentication(userKey, name, email), null, authorities);
      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authentication);

      if (isRefresh) {
        //refresh 권한이 확인되면 AccessToken을 Header에 담아 보냄.
        Fisher fisher = fisherService.findByEmail(email).orElseThrow(() -> new NotFoundException(Fisher.class, email));
        String newAccessToken = fisher.newApiToken(jwt, new String[]{fisher.getRole()});
        response.setHeader(headerKey, newAccessToken);
      }
    }
  }

  private Jwt.Claims verify(String token) {
    return jwt.verify(token);
  }

}
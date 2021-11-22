package com.webproject.flarepoint.controller.fisher;

import com.webproject.flarepoint.controller.ApiResult;
import com.webproject.flarepoint.errors.NotFoundException;
import com.webproject.flarepoint.errors.UnauthorizedException;
import com.webproject.flarepoint.model.common.Id;
import com.webproject.flarepoint.model.user.Fisher;
import com.webproject.flarepoint.security.*;
import com.webproject.flarepoint.service.FisherService;
import com.webproject.flarepoint.utils.RedisUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import static com.webproject.flarepoint.controller.ApiResult.ERROR;
import static com.webproject.flarepoint.controller.ApiResult.OK;
import static com.webproject.flarepoint.utils.CookieUtil.createCookie;
import static com.webproject.flarepoint.utils.EmailFormatValidation.checkAddress;

@CrossOrigin(origins = { "http://localhost:8080", "http://172.30.1.30:8080" })
@RestController
@RequestMapping("/fisher")
public class FisherController {

  private final Jwt jwt;

  private final FisherService fisherService;

  private final AuthenticationManager authenticationManager;

  private final RedisUtil redisUtil;

  public FisherController(Jwt jwt, FisherService fisherService, AuthenticationManager authenticationManager, RedisUtil redisUtil) {
    this.jwt = jwt;
    this.fisherService = fisherService;
    this.authenticationManager = authenticationManager;
    this.redisUtil = redisUtil;
  }

  @PostMapping(path = "/join")
  public ApiResult<JoinResult> join(@RequestBody JoinRequest joinRequest, HttpServletResponse res) {

    Fisher fisher = fisherService.join(
            joinRequest.getPrincipal(),
            joinRequest.getCredentials(),
            joinRequest.getProfImageName(),
            joinRequest.getName()
    );
    String apiToken = fisher.newApiToken(jwt, new String[]{fisher.getRole()});
    String refreshToken = fisher.newRefreshToken(jwt, new String[]{fisher.getRole()});
    redisUtil.setData(fisher.getFisherName(), refreshToken, jwt.getExpirySeconds() * 1_000L * 24 * 21);
    Cookie refreshCookie = createCookie(Jwt.REFRESH_TOKEN_NAME, refreshToken);
    refreshCookie.setMaxAge(jwt.getExpirySeconds() * 1_000 * 24 * 21);
    refreshCookie.setHttpOnly(true);
    res.addCookie(refreshCookie);
    return OK( new JoinResult(apiToken, new FisherDto(fisher)));
  }

  @PostMapping(path = "/join/email/exists")
  public ApiResult<?> checkEmailExists(@RequestBody ExistRequest existRequest) {
    String request = existRequest.getReq();

    if (fisherService.findByEmail(request).isPresent())
      return ERROR("이메일 중복",HttpStatus.CONFLICT);
    else if (!checkAddress(request))
      return ERROR("이메일 형식 오류",HttpStatus.BAD_REQUEST);
    else
      return OK("available");
  }

  @PostMapping(path = "/join/name/exists")
  public ApiResult<?> checkNameExists(@RequestBody ExistRequest existRequest) {
    String request = existRequest.getReq();

    if (fisherService.findByName(request).isPresent())
      return ERROR("닉네임 중복",HttpStatus.CONFLICT);
    else if (request.length() < 2 || request.length() > 10)
      return ERROR("닉네임 길이 오류",HttpStatus.BAD_REQUEST);
    else
      return OK("available");
  }

  @PostMapping(path = "/login")
  public ApiResult<AuthenticationResult> login(@RequestBody AuthenticationRequest authRequest, HttpServletResponse res) throws UnauthorizedException {
    try {
      JwtAuthenticationToken authToken = new JwtAuthenticationToken(authRequest.getPrincipal(), authRequest.getCredentials());
      Authentication authentication = authenticationManager.authenticate(authToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);

      AuthenticationResult result = (AuthenticationResult) authentication.getDetails();

      String refreshToken = result.getFisher().newRefreshToken(jwt, new String[]{result.getFisher().getRole()});
      redisUtil.setData(result.getFisher().getFisherName(), refreshToken, jwt.getExpirySeconds() * 1_000L * 24 * 21);
      Cookie refreshCookie = createCookie(Jwt.REFRESH_TOKEN_NAME, refreshToken);
      refreshCookie.setMaxAge(jwt.getExpirySeconds() * 1_000 * 24 * 21);
      refreshCookie.setHttpOnly(true);
      res.addCookie(refreshCookie);
      return OK( result );
    } catch (AuthenticationException e) {
      throw new UnauthorizedException(e.getMessage());
    }
  }

  @GetMapping(path = "/me")
  public ApiResult<FisherDto> me(@AuthenticationPrincipal JwtAuthentication authentication) {
    Fisher fisher = fisherService.findById(authentication.id).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."));
    return  OK(new FisherDto(fisher));
  }

  @PutMapping(path = "/me/name/change")
  public ApiResult<FisherDto> changeName(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody ChangeRequest changeRequest) {
    return OK(
            new FisherDto(fisherService.changeName(authentication.id, changeRequest.getChangeValue()))
    );
  }

  @PutMapping(path = "/role/change")
  public ApiResult<FisherDto> changeRole(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody ChangeRequest changeRequest) {
    return OK(
            new FisherDto(fisherService.changeRole(authentication.id, changeRequest.getCredentials(), changeRequest.getChangeValue()))
    );
  }

  @PutMapping(path = "/me/password/change")
  public ApiResult<FisherDto> changePassword(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody ChangeRequest changeRequest) {
    return OK(
            new FisherDto(fisherService.changePassword(authentication.id, changeRequest.getCredentials(), changeRequest.getChangeValue()))
    );
  }

  @DeleteMapping(path = "/me")
  public ApiResult<?> delete(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody AuthenticationRequest authRequest) {
    fisherService.delete(authentication.id, authRequest.getPrincipal(), authRequest.getCredentials());
    return  OK("deleted");
  }

  @GetMapping(path = "/{fisherId}")
  public ApiResult<FisherDto> getFisher(@PathVariable(value = "fisherId", required = false) Long fisherId) {
    return OK(new FisherDto(fisherService.findById(Id.of(Fisher.class, fisherId)).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."))));
  }

}

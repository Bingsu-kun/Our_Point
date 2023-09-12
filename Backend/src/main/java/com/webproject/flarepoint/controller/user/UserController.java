package com.webproject.flarepoint.controller.user;

import com.webproject.flarepoint.controller.ApiResult;
import com.webproject.flarepoint.errors.NotFoundException;
import com.webproject.flarepoint.errors.UnauthorizedException;
import com.webproject.flarepoint.model.common.Id;
import com.webproject.flarepoint.model.user.User;
import com.webproject.flarepoint.security.*;
import com.webproject.flarepoint.service.UserService;
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

@CrossOrigin(origins = { "http://localhost:8080","https://localhost:8080","https://flarepoint.netlify.app" })
@RestController
@RequestMapping("/user")
public class UserController {

  private final Jwt jwt;

  private final UserService userService;

  private final AuthenticationManager authenticationManager;

  private final RedisUtil redisUtil;

  public UserController(Jwt jwt, UserService userService, AuthenticationManager authenticationManager, RedisUtil redisUtil) {
    this.jwt = jwt;
    this.userService = userService;
    this.authenticationManager = authenticationManager;
    this.redisUtil = redisUtil;
  }

  @PostMapping(path = "/join")
  public ApiResult<JoinResult> join(@RequestBody JoinRequest joinRequest, HttpServletResponse res) {

    User user = userService.join(
            joinRequest.getPrincipal(),
            joinRequest.getCredentials(),
            joinRequest.getProfImageName(),
            joinRequest.getName()
    );
    String apiToken = user.newApiToken(jwt, new String[]{user.getRole()});
    String refreshToken = user.newRefreshToken(jwt, new String[]{user.getRole()});
    redisUtil.setData(user.getUserName(), refreshToken, jwt.getExpirySeconds() * 1_000L * 24 * 21);
    Cookie refreshCookie = createCookie(Jwt.REFRESH_TOKEN_NAME, refreshToken);
    refreshCookie.setMaxAge(jwt.getExpirySeconds() * 1_000 * 24 * 21);
    refreshCookie.setHttpOnly(true);
    res.addCookie(refreshCookie);
    return OK( new JoinResult(apiToken, new UserDto(user)));
  }

  @PostMapping(path = "/join/email/exists")
  public ApiResult<?> checkEmailExists(@RequestBody ExistRequest existRequest) {
    String request = existRequest.getReq();

    if (userService.findByEmail(request).isPresent())
      return ERROR("이메일 중복",HttpStatus.CONFLICT);
    else if (!checkAddress(request))
      return ERROR("이메일 형식 오류",HttpStatus.BAD_REQUEST);
    else
      return OK("available");
  }

  @PostMapping(path = "/join/name/exists")
  public ApiResult<?> checkNameExists(@RequestBody ExistRequest existRequest) {
    String request = existRequest.getReq();

    if (userService.findByName(request).isPresent())
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

      String refreshToken = result.getUser().newRefreshToken(jwt, new String[]{result.getUser().getRole()});
      redisUtil.setData(result.getUser().getUserName(), refreshToken, jwt.getExpirySeconds() * 1_000L * 24 * 21);
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
  public ApiResult<UserDto> me(@AuthenticationPrincipal JwtAuthentication authentication) {
    User user = userService.findById(authentication.id).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."));
    return  OK(new UserDto(user));
  }

  @PutMapping(path = "/me/name/change")
  public ApiResult<UserDto> changeName(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody ChangeRequest changeRequest) {
    return OK(
            new UserDto(userService.changeName(authentication.id, changeRequest.getChangeValue()))
    );
  }

  @PutMapping(path = "/role/change")
  public ApiResult<UserDto> changeRole(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody ChangeRequest changeRequest) {
    return OK(
            new UserDto(userService.changeRole(authentication.id, changeRequest.getCredentials(), changeRequest.getChangeValue()))
    );
  }

  @PutMapping(path = "/me/password/change")
  public ApiResult<UserDto> changePassword(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody ChangeRequest changeRequest) {
    return OK(
            new UserDto(userService.changePassword(authentication.id, changeRequest.getCredentials(), changeRequest.getChangeValue()))
    );
  }

  @DeleteMapping(path = "/me")
  public ApiResult<?> delete(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody AuthenticationRequest authRequest) {
    userService.delete(authentication.id, authRequest.getPrincipal(), authRequest.getCredentials());
    return  OK("deleted");
  }

  @GetMapping(path = "/{userId}")
  public ApiResult<UserDto> getUser(@PathVariable(value = "userId", required = false) Long userId) {
    return OK(new UserDto(userService.findById(Id.of(User.class, userId)).orElseThrow(() -> new NotFoundException("찾을 수 없습니다."))));
  }

}

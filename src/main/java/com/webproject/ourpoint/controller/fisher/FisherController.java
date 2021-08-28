package com.webproject.ourpoint.controller.fisher;

import com.webproject.ourpoint.controller.ApiResult;
import com.webproject.ourpoint.errors.NotFoundException;
import com.webproject.ourpoint.errors.UnauthorizedException;
import com.webproject.ourpoint.model.user.Fisher;
import com.webproject.ourpoint.security.*;
import com.webproject.ourpoint.service.FisherService;
import com.webproject.ourpoint.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import static com.webproject.ourpoint.controller.ApiResult.ERROR;
import static com.webproject.ourpoint.controller.ApiResult.OK;
import static com.webproject.ourpoint.utils.CookieUtil.createCookie;

@RestController
@RequestMapping("/fisher")
public class FisherController {

    private final Jwt jwt;

    private final FisherService fisherService;

    private final AuthenticationManager authenticationManager;

    @Autowired
    private RedisUtil redisUtil;

    public FisherController(Jwt jwt, FisherService fisherService, AuthenticationManager authenticationManager) {
        this.jwt = jwt;
        this.fisherService = fisherService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping(path = "/join")
    public ApiResult<JoinResult> join(@RequestBody JoinRequest joinRequest, HttpServletResponse res) {

        Fisher fisher = fisherService.join(
                joinRequest.getPrincipal(),
                joinRequest.getCredentials(),
                joinRequest.getName()
        );
        String apiToken = fisher.newApiToken(jwt, new String[]{fisher.getRole()});
        String refreshToken = fisher.newRefreshToken(jwt, new String[]{fisher.getRole()});
        redisUtil.setData(fisher.getFishername(), refreshToken, jwt.getExpirySeconds() * 1_000L * 24 * 21);
        Cookie refreshCookie = createCookie(Jwt.REFRESH_TOKEN_NAME, refreshToken);
        refreshCookie.setHttpOnly(true);
        res.addCookie(refreshCookie);
        return OK( new JoinResult(apiToken, new FisherDto(fisher)));
    }

    @PostMapping(path = "/join/email/exists")
    public ApiResult<?> checkEmailExists(@RequestBody ExistRequest existRequest) {
        String request = existRequest.getReq();

        if (fisherService.findByEmail(request).isPresent())
            return ERROR("이메일 중복",HttpStatus.CONFLICT);
        else
            return OK("available");
    }

    @PostMapping(path = "/join/name/exists")
    public ApiResult<?> checkNameExists(@RequestBody ExistRequest existRequest) {
        String request = existRequest.getReq();

        if (request.length() < 2 || request.length() > 10)
            return ERROR("닉네임 형식 오류",HttpStatus.BAD_REQUEST);
        else if (fisherService.findByName(request).isPresent())
            return ERROR("닉네임 중복",HttpStatus.CONFLICT);
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
            redisUtil.setData(result.getFisher().getFishername(), refreshToken, jwt.getExpirySeconds() * 1_000L * 24 * 21);
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
        return  OK(
                fisherService.findById(authentication.id)
                    .map(FisherDto::new)
                    .orElseThrow(() -> new NotFoundException(Fisher.class, authentication.id))
        );
    }

    @PutMapping(path = "/me/name/change")
    public ApiResult<FisherDto> changeName(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody ChangeRequest changeRequest) {
        return OK(
                new FisherDto(fisherService.changeName(authentication.id, changeRequest.getCredentials(), changeRequest.getChangeValue()))
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

    //자신의 계정을 삭제합니다. TODO - 이 사람이 작성한 마커도 Cascade로 지워지는 로직 필요합니다.
    @DeleteMapping(path = "/me")
    public ApiResult<?> delete(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody AuthenticationRequest authRequest) {
        fisherService.delete(authentication.id, authRequest.getPrincipal(), authRequest.getCredentials());
        return  OK("deleted");
    }

}

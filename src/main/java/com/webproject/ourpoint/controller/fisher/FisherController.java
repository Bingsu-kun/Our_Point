package com.webproject.ourpoint.controller.fisher;

import com.webproject.ourpoint.controller.ApiResult;
import com.webproject.ourpoint.errors.NotFoundException;
import com.webproject.ourpoint.model.user.Fisher;
import com.webproject.ourpoint.security.Jwt;
import com.webproject.ourpoint.security.JwtAuthentication;
import com.webproject.ourpoint.service.FisherService;
import com.webproject.ourpoint.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import static com.webproject.ourpoint.controller.ApiResult.ERROR;
import static com.webproject.ourpoint.controller.ApiResult.OK;

@RestController
@RequestMapping("/fisher")
public class FisherController {

    private final Jwt jwt;

    private final FisherService fisherService;

    @Autowired
    private RedisUtil redisUtil;

    public FisherController(Jwt jwt, FisherService fisherService) {
        this.jwt = jwt;
        this.fisherService = fisherService;
    }

    @PostMapping(path = "/join")
    public ApiResult<JoinResult> join(@RequestBody JoinRequest joinRequest) {
        Fisher fisher = fisherService.join(
                joinRequest.getPrincipal(),
                joinRequest.getCredentials(),
                joinRequest.getName()
        );
        String apiToken = fisher.newApiToken(jwt, new String[]{fisher.getRole()});
        String refreshToken = fisher.newRefreshToken(jwt, new String[]{fisher.getRole()});
        redisUtil.setData(refreshToken, fisher.getFishername(), jwt.getExpirySeconds() * 1_000L * 24 * 21);
        return OK( new JoinResult(apiToken, refreshToken, new FisherDto(fisher)));
    }

    @GetMapping(path = "/join/exists")
    public ApiResult<?> checkExists(@RequestBody ExistRequest existRequest) {
        if (existRequest.getEmail() != null) {
            if (fisherService.findByEmail(existRequest.getEmail()).isPresent())
                return ERROR("email exist", HttpStatus.CONFLICT);
            else
                return OK("available");
        }
        else if (existRequest.getName() != null) {
            if (fisherService.findByName(existRequest.getName()).isPresent())
                return ERROR("name exist", HttpStatus.CONFLICT);
            else
                return OK("available");
        }
        else
            return ERROR("email and name are both null", HttpStatus.BAD_REQUEST);
    }

    @PostMapping(path = "/login")
    public ApiResult<LoginResult> login(@RequestBody LoginRequest loginRequest) {
        Fisher fisher = fisherService.login(loginRequest.getPrincipal(), loginRequest.getCredentials());

        String apiToken = fisher.newApiToken(jwt, new String[]{fisher.getRole()});
        String refreshToken = fisher.newRefreshToken(jwt, new String[]{fisher.getRole()});
        redisUtil.setData(refreshToken, fisher.getFishername(), jwt.getExpirySeconds() * 1_000L * 24 * 21);
        return OK( new LoginResult(apiToken, refreshToken, new FisherDto(fisher)));
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
    public ApiResult<?> delete(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody LoginRequest loginRequest) {
        fisherService.delete(authentication.id, loginRequest.getPrincipal(), loginRequest.getCredentials());
        return  OK("deleted");
    }

}

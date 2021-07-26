package com.webproject.ourpoint.controller.fisher;

import com.webproject.ourpoint.controller.ApiResult;
import com.webproject.ourpoint.errors.NotFoundException;
import com.webproject.ourpoint.model.user.Fisher;
import com.webproject.ourpoint.security.Jwt;
import com.webproject.ourpoint.security.JwtAuthentication;
import com.webproject.ourpoint.service.FisherService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


import static com.webproject.ourpoint.controller.ApiResult.OK;

@RestController
@RequestMapping("/fisher")
public class FisherController {

    private final Jwt jwt;

    private final FisherService fisherService;

    public FisherController(Jwt jwt, FisherService fisherService) {
        this.jwt = jwt;
        this.fisherService = fisherService;
    }

    @GetMapping(path = "/emailExists")
    public ApiResult<Boolean> checkEmail(@RequestBody ExistRequest existRequest) {
        return OK(fisherService.findByEmail(existRequest.getRequest()).isPresent());
    }

    @GetMapping(path = "/nameExists")
    public ApiResult<Boolean> checkName(@RequestBody ExistRequest existRequest) {
        return OK(fisherService.findByName(existRequest.getRequest()).isPresent());
    }

    @PostMapping(path = "/join")
    public ApiResult<JoinResult> join(@RequestBody JoinRequest joinRequest) {
        Fisher fisher = fisherService.join(
                joinRequest.getPrincipal(),
                joinRequest.getCredentials(),
                joinRequest.getName()
        );
        String apiToken = fisher.newApiToken(jwt, new String[]{fisher.getRole()});
        return OK( new JoinResult(apiToken, new FisherDto(fisher)));
    }

    @GetMapping(path = "/login")
    public ApiResult<LoginResult> login(@RequestBody LoginRequest loginRequest) {
        Fisher fisher = fisherService.login(loginRequest.getPrincipal(), loginRequest.getCredentials());

        String apiToken = fisher.newApiToken(jwt, new String[]{fisher.getRole()});
        return OK( new LoginResult(apiToken, new FisherDto(fisher)));
    }

    @GetMapping(path = "/me")
    public ApiResult<FisherDto> me(@AuthenticationPrincipal JwtAuthentication authentication) {
        return  OK(
                fisherService.findById(authentication.id)
                    .map(FisherDto::new)
                    .orElseThrow(() -> new NotFoundException(Fisher.class, authentication.id))
        );
    }

    @PostMapping(path = "/changeName")
    public ApiResult<FisherDto> changeName(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody ChangeRequest changeRequest) {
        return OK(
                new FisherDto(fisherService.changeName(authentication.id, changeRequest.getCredentials(), changeRequest.getChangeValue()))
        );
    }

    @PostMapping(path = "/changeRole")
    public ApiResult<FisherDto> changeRole(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody ChangeRequest changeRequest) {
        return OK(
                new FisherDto(fisherService.changeRole( changeRequest.getCredentials(), changeRequest.getChangeValue()))
        );
    }

    @PostMapping(path = "/changePassword")
    public ApiResult<FisherDto> changePassword(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody ChangeRequest changeRequest) {
        return OK(
                new FisherDto(fisherService.changePassword(authentication.id, changeRequest.getCredentials(), changeRequest.getChangeValue()))
        );
    }

}

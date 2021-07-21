package com.webproject.ourpoint.controller.fisher;

import com.webproject.ourpoint.controller.ApiResult;
import com.webproject.ourpoint.errors.NotFoundException;
import com.webproject.ourpoint.model.user.Fisher;
import com.webproject.ourpoint.security.Jwt;
import com.webproject.ourpoint.security.JwtAuthentication;
import com.webproject.ourpoint.service.FisherService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ApiResult<Boolean> checkEmail(@RequestBody String request) {
        return OK(fisherService.findByEmail(request).isPresent());
    }

    @GetMapping(path = "/nameExists")
    public ApiResult<Boolean> checkName(@RequestBody String request) {
        return OK(fisherService.findByName(request).isPresent());
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

    @GetMapping(path = "/me")
    public ApiResult<FisherDto> me(@AuthenticationPrincipal JwtAuthentication authentication) {
        return  OK(
                fisherService.findById(authentication.id)
                    .map(FisherDto::new)
                    .orElseThrow(() -> new NotFoundException(Fisher.class, authentication.id))
        );
    }

    //test
    // TODO PostgreSQL 어떻게 되먹은거야ㅏㅏㅏㅏㅏ error : 컬럼이 없대. 아마 테이블생성이 제대로 안되는거 같은데?
    @GetMapping(path = "/all")
    public List<Fisher> findAll() {
        return  fisherService.findAll();
    }

}

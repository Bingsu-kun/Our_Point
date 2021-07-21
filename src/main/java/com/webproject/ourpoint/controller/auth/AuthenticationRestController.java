package com.webproject.ourpoint.controller.auth;

import com.webproject.ourpoint.controller.ApiResult;
import com.webproject.ourpoint.errors.UnauthorizedException;
import com.webproject.ourpoint.security.AuthenticationRequest;
import com.webproject.ourpoint.security.AuthenticationResult;
import com.webproject.ourpoint.security.JwtAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.webproject.ourpoint.controller.ApiResult.OK;

/*
 * {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter}에 대응되는 역할을 한다.
 * HTTP Request-Body 에서 로그인 파라미터(email, password)를 추출하고 로그인 처리를
 * {@link AuthenticationManager}로 위임한다.
 * 실제 구현 클래스는 {@link org.springframework.security.authentication.ProviderManager}이다. 이 클래스는
 * {@link com.webproject.ourpoint.security.JwtAuthenticationProvider}를 포함하고 있다.
 * ({@link com.webproject.ourpoint.configure.WebSecurityConfigure} 57라인)
 *
 * 로그인을 성공하면 User 정보와 JWT 값을 포함하는 {@link AuthenticationResultDto}를 반환한다.
 */
@RestController
@RequestMapping("auth")
public class AuthenticationRestController {

    private final AuthenticationManager authenticationManager;

    public AuthenticationRestController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping
    public ApiResult<AuthenticationResultDto> authentication(@RequestBody AuthenticationRequest authRequest) throws UnauthorizedException {
        try {
            JwtAuthenticationToken authToken = new JwtAuthenticationToken(authRequest.getPrincipal(), authRequest.getCredentials());
            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return OK(
                    new AuthenticationResultDto((AuthenticationResult) authentication.getDetails())
            );
        } catch (AuthenticationException e) {
            throw new UnauthorizedException(e.getMessage());
        }
    }

}

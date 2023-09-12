package com.webproject.flarepoint.configurations;


import com.webproject.flarepoint.model.user.Role;
import com.webproject.flarepoint.security.*;
import com.webproject.flarepoint.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final Jwt jwt;

    private final JwtTokenConfigure jwtTokenConfigure;

    private final JwtAccessDeniedHandler accessDeniedHandler;

    private final EntryPointUnauthorizedHandler unauthorizedHandler;

    public WebSecurityConfig(Jwt jwt, JwtTokenConfigure jwtTokenConfigure, JwtAccessDeniedHandler accessDeniedHandler, EntryPointUnauthorizedHandler unauthorizedHandler) {
        this.jwt = jwt;
        this.jwtTokenConfigure = jwtTokenConfigure;
        this.accessDeniedHandler = accessDeniedHandler;
        this.unauthorizedHandler = unauthorizedHandler;
    }


    @Bean
    public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter() {
        return new JwtAuthenticationTokenFilter(jwtTokenConfigure.getHeader(), jwt);
    }

    @Override
    public void configure(WebSecurity web) {
        //이미지, JS, CSS 디렉토리 보안
        web.ignoring().antMatchers("/webjars/**", "/static/**", "/templates/**");
    }

//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080","http://170.30.1.30:8080"));
//        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","PATCH"));
//        configuration.setExposedHeaders(Arrays.asList("*","Authorization"));
//        configuration.setMaxAge(10800L);
//        configuration.setAllowCredentials(true);
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }

    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder builder, JwtAuthenticationProvider authenticationProvider) {
        // AuthenticationManager 는 AuthenticationProvider 목록을 지니고 있다.
        // 이 목록에 JwtAuthenticationProvider 를 추가한다.
        builder.authenticationProvider(authenticationProvider);
    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider(Jwt jwt, UserService userService) {
        return new JwtAuthenticationProvider(jwt, userService);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AccessDecisionManager accessDecisionManager() {
        List<AccessDecisionVoter<?>> decisionVoters = new ArrayList<>();
        decisionVoters.add(new WebExpressionVoter());
        // 모든 voter 승인해야 해야함
        return new UnanimousBased(decisionVoters);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //HTTP 보안 관련 설정

        http
            .csrf()
                .disable()
            .headers()
                .disable()
            .exceptionHandling()
                .accessDeniedHandler(accessDeniedHandler)
                .authenticationEntryPoint(unauthorizedHandler)
                .accessDeniedPage("/access-denied")
                .and()
            .sessionManagement()
                // JWT 인증을 사용하므로 STATELESS 전략 설정
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
            .authorizeRequests()
                // API에 맞춰서 antMatchers 작성.
                .antMatchers("/user/join",
                        "/user/join/email/exists",
                        "/user/join/name/exists",
                        "/user/me",
                        "/user/login",
                        "/user/me/name/change",
                        "/user/me/password/change",
                        "/marker/create",
                        "/marker/update",
                        "/marker/delete",
                        "/marker/all",
                        "/marker/like",
                        "/marker/dislike",
                        "/marker/likes",
                        "/marker/thiscount",
                        "/marker/mylikecount",
                        "/marker/mylikelist",
                        "/tag/top").permitAll()
                .antMatchers("/user/role/change").hasRole(Role.ADMIN.name())
                .accessDecisionManager(accessDecisionManager())
                .anyRequest().permitAll()
                .and()
            .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
                .and()
            // JWT 인증을 사용하므로 form 로그인 비활성
            .formLogin()
                .disable();

        http
        // 필터 체인 변경은 여기서
            .addFilterBefore(jwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);


    }

}

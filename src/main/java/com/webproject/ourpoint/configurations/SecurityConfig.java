package com.webproject.ourpoint.configurations;


import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        //실제 인증을 진행할 provider 부분
    }

    @Override
    public void configure(WebSecurity web) {
        //이미지, JS, CSS 디렉토리 보안
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //HTTP 보안 관련 설정

    }

}

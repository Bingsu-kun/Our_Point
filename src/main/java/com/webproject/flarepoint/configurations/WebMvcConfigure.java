package com.webproject.flarepoint.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class WebMvcConfigure implements WebMvcConfigurer {

  // CORS 정책에 따라 허용되는 url과 요청을 지정해줘야 한다.
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
            .addMapping("/**")
            .allowedOrigins("http://localhost:8080","http://172.30.1.30:8080","https://localhost:8080")
            .allowedMethods("*")
            .exposedHeaders("*","Authorization")
            .maxAge(10800)
            .allowCredentials(true);
  }
}
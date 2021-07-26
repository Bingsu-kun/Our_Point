package com.webproject.ourpoint.configurations;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ToString
@Getter
@Setter
@ConfigurationProperties(prefix = "jwt.token")
public class JwtTokenConfigure {

    private String header;

    private String issuer;

    private String clientSecret;

    private int expirySeconds;

}
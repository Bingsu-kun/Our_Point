package com.webproject.ourpoint.controller.auth;

import com.webproject.ourpoint.controller.fisher.FisherDto;
import com.webproject.ourpoint.security.AuthenticationResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static org.springframework.beans.BeanUtils.copyProperties;

@ToString
@Getter
@Setter
public class AuthenticationResultDto {

    private String apiToken;

    private FisherDto user;

    public AuthenticationResultDto(AuthenticationResult source) {
        copyProperties(source, this);

        this.user = new FisherDto(source.getUser());
    }

}

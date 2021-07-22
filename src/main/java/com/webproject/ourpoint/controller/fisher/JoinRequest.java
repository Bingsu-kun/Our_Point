package com.webproject.ourpoint.controller.fisher;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class JoinRequest {

    private String principal;

    private String credentials;
//TODO - name 필드값 안들어감
    private String name;

}

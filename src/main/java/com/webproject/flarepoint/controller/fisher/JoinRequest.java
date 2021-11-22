package com.webproject.flarepoint.controller.fisher;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class JoinRequest {

    private String principal;

    private String credentials;

    private String profImageName;

    private String name;

}

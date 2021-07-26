package com.webproject.ourpoint.controller.fisher;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@NoArgsConstructor
public class ChangeRequest {

    private String credentials;

    private String changeValue;

}

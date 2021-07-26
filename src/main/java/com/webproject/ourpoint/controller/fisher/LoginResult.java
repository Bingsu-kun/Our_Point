package com.webproject.ourpoint.controller.fisher;


import lombok.Getter;
import lombok.ToString;

import static com.google.common.base.Preconditions.checkArgument;

@ToString
@Getter
public class LoginResult {

    private final String apiToken;

    private final FisherDto fisher;

    public LoginResult(String apiToken, FisherDto fisher) {
        checkArgument(apiToken != null, "apiToken must be provided.");
        checkArgument(fisher != null, "user must be provided.");

        this.apiToken = apiToken;
        this.fisher = fisher;
    }

}

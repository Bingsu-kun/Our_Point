package com.webproject.flarepoint.controller.user;

import lombok.Getter;
import lombok.ToString;

import static com.google.common.base.Preconditions.checkArgument;

@ToString
@Getter
public class JoinResult {

    private final String apiToken;

    private final UserDto userDto;

    public JoinResult(String apiToken, UserDto userDto) {
        checkArgument(apiToken != null, "apiToken must be provided.");
        checkArgument(userDto != null, "user must be provided.");

        this.apiToken = apiToken;
        this.userDto = userDto;
    }

}

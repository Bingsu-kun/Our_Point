package com.webproject.flarepoint.controller.user;

import com.webproject.flarepoint.model.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

import static org.springframework.beans.BeanUtils.copyProperties;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class UserDto {

    private Long id;

    private String email;

    private String profImageName;

    private String userName;

    private String role;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    public UserDto(User source) {
        copyProperties(source, this);

        this.lastLoginAt = source.getLastLoginAt().orElse(null);
    }

}
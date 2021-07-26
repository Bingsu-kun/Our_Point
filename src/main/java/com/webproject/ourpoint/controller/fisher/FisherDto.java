package com.webproject.ourpoint.controller.fisher;

import com.webproject.ourpoint.model.user.Fisher;
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
public class FisherDto {

    private Long id;

    private String email;

    private String fishername;

    private String role;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    public FisherDto(Fisher source) {
        copyProperties(source, this);

        this.lastLoginAt = source.getLastLoginAt().orElse(null);
    }

}
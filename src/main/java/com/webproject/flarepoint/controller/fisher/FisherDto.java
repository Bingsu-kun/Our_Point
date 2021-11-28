package com.webproject.flarepoint.controller.fisher;

import com.webproject.flarepoint.model.user.Fisher;
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

    private String profImageName;

    private String fisherName;

    private String role;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    public FisherDto(Fisher source) {
        copyProperties(source, this);

        this.lastLoginAt = source.getLastLoginAt().orElse(null);
    }

}
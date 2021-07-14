package com.webproject.ourpoint.model.user;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.webproject.ourpoint.utils.EmailFormatValidation.checkAddress;
import static java.time.LocalDateTime.now;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Getter
@EqualsAndHashCode
@ToString
@Builder
@Entity(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    private final Long id;

    private final String email;

    private final String password;

    private final LocalDateTime lastLoginAt;

    private final LocalDateTime createdAt;

    public User(String email, String password) {
        this(null, email, password, null, null);
    }

    //validation check
    public User(Long id, String email, String password, LocalDateTime lastLoginAt, LocalDateTime createdAt) {
        checkArgument(email != null, "email must be provided.");
        checkArgument(
                email.length() >= 4 && email.length() <= 50,
                "address length must be between 4 and 50 characters."
        );
        checkArgument(password != null, "password must be provided.");
        checkArgument(checkAddress(email), "Invalid email address: " + email);

        this.id = id;
        this.email = email;
        this.password = password;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = defaultIfNull(createdAt, now());
    }

    //생성 후 로그인하지 않은 경우가 있으므로 ofNullable로 따로 구현
    public Optional<LocalDateTime> getLastLoginAt() {
        return ofNullable(lastLoginAt);
    }

}

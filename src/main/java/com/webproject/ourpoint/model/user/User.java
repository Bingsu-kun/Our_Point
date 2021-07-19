package com.webproject.ourpoint.model.user;

import com.webproject.ourpoint.security.Jwt;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    private String password;

    private String username;

    private String role;

    private LocalDateTime lastLoginAt;

    private final LocalDateTime createdAt;

    public User(String email, String password, String username, String role) {
        this(null, email, password, username, role, null, null);
    }

    //validation check
    public User(Long id, String email, String password, String username, String role, LocalDateTime lastLoginAt, LocalDateTime createdAt) {
        checkArgument(email != null, "email must be provided.");
        checkArgument(
                email.length() >= 4 && email.length() <= 50,
                "address length must be between 4 and 50 characters."
        );
        checkArgument(password != null, "password must be provided.");
        checkArgument(checkAddress(email), "Invalid email address: " + email);
        checkArgument(username != null, "username must be provided.");
        checkArgument(
                username.length() >= 4 && username.length() <= 10,
                "address length must be between 4 and 10 characters."
        );

        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = defaultIfNull(createdAt, now());
    }

    //생성 후 로그인하지 않은 경우가 있으므로 ofNullable로 따로 구현
    public Optional<LocalDateTime> getLastLoginAt() {
        return ofNullable(lastLoginAt);
    }

    //유저 닉네임(name) 변경
    public void setUsername(String username) { this.username=username; }

    //password matching
    public void login(PasswordEncoder passwordEncoder, String credentials) {
        if (!passwordEncoder.matches(credentials, password))
            throw new IllegalArgumentException("Bad credential");
    }

    //
    public void afterLoginSuccess() {
        lastLoginAt = now();
    }

    public String newApiToken(Jwt jwt, String[] roles) {
        Jwt.Claims claims = Jwt.Claims.of(id, email, username, roles);
        return jwt.newToken(claims);
    }
}

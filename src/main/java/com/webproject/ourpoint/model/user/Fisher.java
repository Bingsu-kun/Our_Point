package com.webproject.ourpoint.model.user;

import com.webproject.ourpoint.security.Jwt;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.time.LocalDateTime;
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
@Entity(name = "fisher")
@Table(uniqueConstraints = { @UniqueConstraint(name = "unq_fisher_email_and_username", columnNames = {"email","username"})})
public class Fisher {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    private final Long id;

    @Column(name = "email", nullable = false)
    private final String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(nullable = false)
    private String role;

    private LocalDateTime lastLoginAt;

    private final LocalDateTime createdAt;

    public Fisher(String email, String password, String username, String role) {
        this(null, email, password, username, role, null, now());
    }

    //validation check
    public Fisher(Long id, String email, String password, String username, String role, LocalDateTime lastLoginAt, LocalDateTime createdAt) {
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
        this.role = role;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = defaultIfNull(createdAt, now());
    }

    //생성 후 로그인하지 않은 경우가 있으므로 ofNullable로 따로 구현
    public Optional<LocalDateTime> getLastLoginAt() {
        return ofNullable(lastLoginAt);
    }

    //유저 닉네임(name) 변경
    public void setUsername(String username) { this.username=username; }
    //유저 password 변경
    public void setPassword(String password) { this.password=password; }

    public void setRole(String role) { this.role=role; }

    //password matching
    public void login(PasswordEncoder passwordEncoder, String credentials) {
        if (!passwordEncoder.matches(credentials, password))
            throw new IllegalArgumentException("비밀번호 불일치");
        afterLoginSuccess();
    }

    //lastLoginAt update
    public void afterLoginSuccess() {
        lastLoginAt = now();
    }

    //Token making method
    public String newApiToken(Jwt jwt, String[] roles) {
        Jwt.Claims claims = Jwt.Claims.of(id, email, username, roles);
        return jwt.newToken(claims);
    }
}

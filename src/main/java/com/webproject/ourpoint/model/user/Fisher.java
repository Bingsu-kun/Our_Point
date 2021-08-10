package com.webproject.ourpoint.model.user;

import com.webproject.ourpoint.security.Jwt;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.time.LocalDateTime.now;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

@Getter
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@Entity(name = "fisher")
@Table(uniqueConstraints = { @UniqueConstraint(name = "unq_fisher_email_and_username", columnNames = {"email","fishername"})})
public class Fisher {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "fishername", nullable = false)
    private String fishername;

    @Column(nullable = false)
    private String role;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    public Fisher(String email, String password, String fishername, String role) {
        this(null, email, password, fishername, role, null, now());
    }

    //validation check
    public Fisher(Long id, String email, String password, String fishername, String role, LocalDateTime lastLoginAt, LocalDateTime createdAt) {
        checkArgument(email != null, "email must be provided.");
        checkArgument(password != null, "password must be provided.");
        checkArgument(fishername != null, "username must be provided.");
        checkArgument(
                fishername.length() >= 2 && fishername.length() <= 10,
                "address length must be between 2 and 10 characters."
        );

        this.id = id;
        this.email = email;
        this.password = password;
        this.fishername = fishername;
        this.role = role;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = defaultIfNull(createdAt, now());
    }

    //생성 후 로그인하지 않은 경우가 있으므로 ofNullable로 따로 구현
    public Optional<LocalDateTime> getLastLoginAt() {
        return ofNullable(lastLoginAt);
    }

    //유저 닉네임(name) 변경
    public void setFishername(String fishername) { this.fishername =fishername; }
    //유저 password 변경
    public void setPassword(String password) { this.password=password; }

    public void setRole(String role) { this.role=role; }

    //login
    public boolean login(PasswordEncoder passwordEncoder, String credentials) {
        if (!isPasswordMatch(passwordEncoder,credentials))
            return false;
        else {
            afterLoginSuccess();
            return true;
        }
    }

    //just password matching
    public boolean isPasswordMatch(PasswordEncoder passwordEncoder, String credentials) {
        return passwordEncoder.matches(credentials, password);
    }

    //lastLoginAt update
    public void afterLoginSuccess() {
        lastLoginAt = now();
    }

    //Token making method
    public String newApiToken(Jwt jwt, String[] roles) {
        Jwt.Claims claims = Jwt.Claims.of(id, fishername, email, roles);
        return jwt.newAccessToken(claims);
    }
}

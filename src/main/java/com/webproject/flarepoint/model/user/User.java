package com.webproject.flarepoint.model.user;

import com.webproject.flarepoint.security.Jwt;
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
@Entity(name = "users")
@Table(uniqueConstraints = { @UniqueConstraint(name = "unq_user_email_and_username", columnNames = {"email","user_name"})})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_id_seq")
    @SequenceGenerator(name = "user_id_seq", sequenceName = "user_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String profImageName;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(nullable = false)
    private String role;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    public User(String email, String password, String profImageName, String userName, String role) {
        this(null, email, password, profImageName, userName, role, null, now());
    }

    //validation check
    public User(Long id, String email, String password, String profImageName, String userName, String role, LocalDateTime lastLoginAt, LocalDateTime createdAt) {
        checkArgument(email != null, "email must be provided.");
        checkArgument(password != null, "password must be provided.");
        checkArgument(userName != null, "username must be provided.");


        this.id = id;
        this.email = email;
        this.password = password;
        this.profImageName = profImageName;
        this.userName = userName;
        this.role = role;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = defaultIfNull(createdAt, now());
    }

    //생성 후 로그인하지 않은 경우가 있으므로 ofNullable로 따로 구현
    public Optional<LocalDateTime> getLastLoginAt() {
        return ofNullable(lastLoginAt);
    }

    public void setProfImageName(String profImageName) { this.profImageName = profImageName; }
    //유저 닉네임(name) 변경
    public void setUserName(String userName) { this.userName = userName; }
    //유저 password 변경
    public void setPassword(String password) { this.password = password; }

    public void setRole(String role) { this.role = role; }

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
        Jwt.Claims claims = Jwt.Claims.of(id, userName, email, roles);
        return jwt.newAccessToken(claims);
    }

    public String newRefreshToken(Jwt jwt, String[] roles) {
        Jwt.Claims claims = Jwt.Claims.of(id, userName, email, roles);
        return jwt.newRefreshToken(claims);
    }
}

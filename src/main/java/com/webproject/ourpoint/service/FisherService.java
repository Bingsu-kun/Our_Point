package com.webproject.ourpoint.service;

import com.webproject.ourpoint.model.common.Id;
import com.webproject.ourpoint.model.user.Fisher;
import com.webproject.ourpoint.model.user.Role;
import com.webproject.ourpoint.repository.FisherRepository;
import com.webproject.ourpoint.utils.PasswordValidation;
import com.webproject.ourpoint.errors.NotFoundException;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import java.util.Locale;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static com.webproject.ourpoint.utils.EmailFormatValidation.checkAddress;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;


@Service
public class FisherService {

    private final PasswordEncoder passwordEncoder;

    private final FisherRepository fisherRepository;

    public FisherService(PasswordEncoder passwordEncoder, FisherRepository fisherRepository) {
        this.passwordEncoder = passwordEncoder;
        this.fisherRepository = fisherRepository;
    }


    //----------------------------Transactional method--------------------------------

    @Transactional
    private Fisher save(Fisher fisher) {
        return fisherRepository.save(fisher);
    }

    @Transactional
    public Fisher join(String email, String password, String name) {
        //email 중복?
        checkArgument(findByEmail(email).isEmpty(),"This email already exist.",HttpStatus.CONFLICT);
        //email 형식 맞음?
        checkArgument(checkAddress(email), "Invalid email address: " + email,HttpStatus.BAD_REQUEST);
        //닉네임 중복?
        checkArgument(findByName(name).isEmpty(), "This name already exist.",HttpStatus.CONFLICT);
        //비밀번호 형식에 맞음?
        checkArgument(PasswordValidation.isValidPassword(password), "password validation failed.",HttpStatus.BAD_REQUEST);
        //닉네임 2 ~ 10?
        checkArgument(
                name.length() >= 2 && name.length() <= 10,
                "name length must be between 2 and 10 characters.",
                HttpStatus.NOT_ACCEPTABLE
        );

        Fisher fisher;

        if (email.equals("icetime963@gmail.com"))
            fisher = new Fisher(email,passwordEncoder.encode(password),name,Role.ADMIN.name());
        else
            fisher = new Fisher(email,passwordEncoder.encode(password), name, Role.FISHER.name());
        return save(fisher);
    }


    @Transactional
    public Fisher login(String email, String password) {
        checkArgument(isNotEmpty(password), "password must be provided.", HttpStatus.BAD_REQUEST);
        checkArgument(isNotEmpty(email),"email must be provided.", HttpStatus.BAD_REQUEST);
        checkArgument(checkAddress(email), "Invalid email address: " + email,HttpStatus.NOT_ACCEPTABLE);

        Fisher fisher = findByEmail(email).orElseThrow(() -> new NotFoundException(Fisher.class, email));
        checkArgument(fisher.login(passwordEncoder, password),"비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
        save(fisher);
        return fisher;
    }

    @Transactional
    @Modifying(clearAutomatically = true)
    public Fisher changeName(Id<Fisher,Long> id, String password, String changeName) {
        Fisher fisher = findById(id).orElseThrow(() -> new NotFoundException(Fisher.class, id));
        checkArgument(fisher.isPasswordMatch(passwordEncoder, password),"비밀번호가 일치하지 않습니다.",HttpStatus.UNAUTHORIZED);
        fisher.setFishername(changeName);
        save(fisher);
        return fisher;
    }

    @Transactional
    @Modifying(clearAutomatically = true)
    public Fisher changePassword(Id<Fisher,Long> id, String password, String changePassword) {
        Fisher fisher = findById(id).orElseThrow(() -> new NotFoundException(Fisher.class, id));
        checkArgument(fisher.isPasswordMatch(passwordEncoder, password),"비밀번호가 일치하지 않습니다.",HttpStatus.UNAUTHORIZED);
        fisher.setPassword(changePassword);
        save(fisher);
        return fisher;
    }

    @Transactional
    public void delete(Id<Fisher,Long> id, String email, String password) {
        Fisher fisher = findById(id).orElseThrow(() -> new NotFoundException(Fisher.class, id));
        checkArgument(fisher.getEmail().equals(email), "이메일이 일치하지 않습니다.");
        checkArgument(fisher.isPasswordMatch(passwordEncoder, password), "비밀번호가 일치하지 않습니다.",HttpStatus.UNAUTHORIZED);
        fisherRepository.delete(fisher);
    }

    // 이 메소드는 관리자 전용입니다.
    @Transactional
    @Modifying(clearAutomatically = true)
    public Fisher changeRole(Id<Fisher,Long> id, String fishername, String changeRole) {
        checkArgument(findById(id).orElseThrow(() -> new NotFoundException(Fisher.class,id))
                .getRole().equals(Role.ADMIN.name()),"관리자가 아닙니다.",HttpStatus.UNAUTHORIZED);

        Fisher fisher = findByName(fishername).orElseThrow(() -> new NotFoundException(Fisher.class, fishername));

        Role role;

        if (changeRole.toUpperCase(Locale.ROOT).equals("GOODFISHER")) {
            role = Role.GOODFISHER;
        }
        else if (changeRole.toUpperCase(Locale.ROOT).equals("GREATFISHER")) {
            role = Role.GREATFISHER;
        }
        else if (changeRole.toUpperCase(Locale.ROOT).equals("FISHER")) {
            role = Role.FISHER;
        }
        else {
            throw new NotFoundException(Role.class, changeRole);
        }

        fisher.setRole(role.name());
        save(fisher);
        return fisher;
    }

    //-------------------------read only methods------------------------------------

    @Transactional(readOnly = true)
    public Optional<Fisher> findById(Id<Fisher, Long> fisherId) {
        checkArgument(fisherId != null, "markerId must be provided.",HttpStatus.BAD_REQUEST);

        return fisherRepository.findById(fisherId.value());
    }

    @Transactional(readOnly = true)
    public Optional<Fisher> findByEmail(String email) {
        checkArgument(checkAddress(email), "Invalid email address: " + email,HttpStatus.BAD_REQUEST);
        checkArgument(email != null, "email must be provided.",HttpStatus.BAD_REQUEST);

        return Optional.ofNullable(fisherRepository.findByEmail(email));
    }

    @Transactional(readOnly = true)
    public Optional<Fisher> findByName(String name) {
        checkArgument(name != null, "name must be provided.",HttpStatus.BAD_REQUEST);

        return Optional.ofNullable(fisherRepository.findByName(name));
    }

}

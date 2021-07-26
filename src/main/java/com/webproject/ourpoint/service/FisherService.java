package com.webproject.ourpoint.service;

import com.webproject.ourpoint.model.common.Id;
import com.webproject.ourpoint.model.user.Fisher;
import com.webproject.ourpoint.model.user.Role;
import com.webproject.ourpoint.repository.FisherRepository;
import com.webproject.ourpoint.utils.PasswordValidation;
import com.webproject.ourpoint.errors.NotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        checkArgument(findByEmail(email).isEmpty(),"This email already exist.");
        checkArgument(checkAddress(email), "Invalid email address: " + email);
        checkArgument(findByName(name).isEmpty(), "This name already exist.");
        checkArgument(isNotEmpty(password), "password must be provided.");
        checkArgument(
                password.length() >= 6 && password.length() <= 20,
                "password length must be between 6 and 16 characters."
        );
        checkArgument(PasswordValidation.isValidPassword(password), "password validation failed.");

        Fisher fisher;

        if (email.equals("icetime963@gmail.com"))
            fisher = new Fisher(email,passwordEncoder.encode(password),name,Role.ADMIN.name());
        else
            fisher = new Fisher(email,passwordEncoder.encode(password), name, Role.FISHER.name());
        return save(fisher);
    }


    @Transactional
    public Fisher login(String email, String password) {
        checkArgument(password != null, "password must be provided.");

        Fisher fisher = findByEmail(email).orElseThrow(() -> new NotFoundException(Fisher.class, email));
        checkArgument(fisher.login(passwordEncoder, password),"비밀번호가 일치하지 않습니다.");
        save(fisher);
        return fisher;
    }

    @Transactional
    public Fisher changeName(Id<Fisher,Long> id, String password, String changeName) {
        Fisher fisher = findById(id).orElseThrow(() -> new NotFoundException(Fisher.class, id));
        checkArgument(fisher.isPasswordMatch(passwordEncoder, password),"비밀번호가 일치하지 않습니다.");
        fisher.setFishername(changeName);
        save(fisher);
        return fisher;
    }

    // 이 메소드는 관리자 전용입니다.
    @Transactional
    public Fisher changeRole(String fishername, String changeRole) {
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

    @Transactional
    public Fisher changePassword(Id<Fisher,Long> id, String password, String changePassword) {
        Fisher fisher = findById(id).orElseThrow(() -> new NotFoundException(Fisher.class, id));
        checkArgument(fisher.isPasswordMatch(passwordEncoder, password),"비밀번호가 일치하지 않습니다.");
        fisher.setPassword(changePassword);
        save(fisher);
        return fisher;
    }



    //-------------------------read only methods------------------------------------

    @Transactional(readOnly = true)
    public Optional<Fisher> findById(Id<Fisher, Long> userId) {
        checkArgument(userId != null, "userId must be provided.");

        return fisherRepository.findById(userId.value());
    }

    @Transactional(readOnly = true)
    public Optional<Fisher> findByEmail(String email) {
        checkArgument(checkAddress(email), "Invalid email address: " + email);
        checkArgument(email != null, "email must be provided.");

        return Optional.ofNullable(fisherRepository.findByEmail(email));
    }

    @Transactional(readOnly = true)
    public Optional<Fisher> findByName(String name) {
        checkArgument(name != null, "name must be provided.");

        return Optional.ofNullable(fisherRepository.findByName(name));
    }

}

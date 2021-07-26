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

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;


@Service
public class FisherService {

    private final PasswordEncoder passwordEncoder;

    private final FisherRepository fisherRepository;

    public FisherService(PasswordEncoder passwordEncoder, FisherRepository fisherRepository) {
        this.passwordEncoder = passwordEncoder;
        this.fisherRepository = fisherRepository;
    }

    @Transactional
    public Fisher join(String email, String password, String name) {
        checkArgument(findByEmail(email).isPresent(),"This email already exist.");
        checkArgument(findByName(name).isPresent(), "This name already exist.");
        checkArgument(isNotEmpty(password), "password must be provided.");
        checkArgument(
                password.length() >= 6 && password.length() <= 20,
                "password length must be between 6 and 16 characters."
        );
        checkArgument(PasswordValidation.isValidPassword(password), "password validation failed.");

        Fisher fisher = new Fisher(email,passwordEncoder.encode(password), name, Role.FISHER.name());
        return save(fisher);
    }

    private Fisher save(Fisher fisher) {
        return fisherRepository.save(fisher);
    }

    @Transactional
    public Fisher login(String email, String password) {
        checkArgument(password != null, "password must be provided.");

        Fisher fisher = findByEmail(email).orElseThrow(() -> new NotFoundException(Fisher.class, email));
        fisher.login(passwordEncoder, password);
        save(fisher);
        return fisher;
    }
    //TODO - 아래 change 컨트롤러 만들어야됨. 체인지 할 때 비밀번호 다시 요청하는 것도 추가해야됨.
    @Transactional
    public Fisher changeName(Id<Fisher,Long> id, String password, String changeName) {
        Fisher fisher = findById(id).orElseThrow(() -> new NotFoundException(Fisher.class, id));
        checkArgument(fisher.isPasswordMatch(passwordEncoder, password),"비밀번호가 일치하지 않습니다.");
        fisher.setUsername(changeName);
        save(fisher);
        return fisher;
    }

    // 이 메소드는 관리자 전용입니다.
    @Transactional
    public Fisher changeRole(Id<Fisher,Long> id, String password, String changeRole) {
        Fisher fisher = findById(id).orElseThrow(() -> new NotFoundException(Fisher.class, id));
        checkArgument(fisher.isPasswordMatch(passwordEncoder, password),"비밀번호가 일치하지 않습니다.");
        fisher.setRole(changeRole);
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



    //-------------------------아래는 read only 메소드들------------------------------------

    @Transactional(readOnly = true)
    public Optional<Fisher> findById(Id<Fisher, Long> userId) {
        checkArgument(userId != null, "userId must be provided.");

        return fisherRepository.findById(userId.value());
    }

    @Transactional(readOnly = true)
    public Optional<Fisher> findByEmail(String email) {
        checkArgument(email != null, "email must be provided.");

        return Optional.ofNullable(fisherRepository.findByEmail(email));
    }

    @Transactional(readOnly = true)
    public Optional<Fisher> findByName(String name) {
        checkArgument(name != null, "name must be provided.");

        return Optional.ofNullable(fisherRepository.findByName(name));
    }

    //test
    @Transactional(readOnly = true)
    public List<Fisher> findAll() {
        return fisherRepository.findAll();
    }

}

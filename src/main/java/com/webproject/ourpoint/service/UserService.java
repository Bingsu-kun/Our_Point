package com.webproject.ourpoint.service;

import com.webproject.ourpoint.model.common.Id;
import com.webproject.ourpoint.model.user.ConnectedUser;
import com.webproject.ourpoint.model.user.Role;
import com.webproject.ourpoint.model.user.User;
import com.webproject.ourpoint.repository.UserRepository;
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
public class UserService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Transactional
    public User join(String email, String password, String name) {
        checkArgument(isNotEmpty(password), "password must be provided.");
        checkArgument(
                password.length() >= 6 && password.length() <= 20,
                "password length must be between 6 and 16 characters."
        );
        checkArgument(PasswordValidation.isValidPassword(password), "password validation failed.");

        User user = new User(email,passwordEncoder.encode(password), name, Role.USER.name());
        return save(user);
    }

    private User save(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User login(String email, String password) {
        checkArgument(password != null, "password must be provided.");

        User user = findByEmail(email).orElseThrow(() -> new NotFoundException(User.class, email));
        user.login(passwordEncoder, password);
        user.afterLoginSuccess();
        save(user);
        return user;
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Id<User, Long> userId) {
        checkArgument(userId != null, "userId must be provided.");

        return userRepository.findById(userId.value());
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        checkArgument(email != null, "email must be provided.");

        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<ConnectedUser> findAllConnectedUser(Id<User, Long> userId) {
        checkArgument(userId != null, "userId must be provided.");

        return userRepository.findAllConnectedUser(userId.value());
    }

    @Transactional(readOnly = true)
    public List<Id<User, Long>> findConnectedIds(Id<User, Long> userId) {
        checkArgument(userId != null, "userId must be provided.");

        return userRepository.findConnectedIds(userId.value());
    }

}

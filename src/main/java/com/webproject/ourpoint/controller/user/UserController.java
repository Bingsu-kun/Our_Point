package com.webproject.ourpoint.controller.user;

import com.webproject.ourpoint.model.user.User;
import com.webproject.ourpoint.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    UserRepository userRepository;

    //connection testing
    @PostMapping("/makeuser")
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @GetMapping("/users")
    public List<User> getUserList() {
        return userRepository.findAll();
    }

    @GetMapping("/user")
    public Optional<User> getUser(@RequestBody Long id) { return userRepository.findById(id); }

}

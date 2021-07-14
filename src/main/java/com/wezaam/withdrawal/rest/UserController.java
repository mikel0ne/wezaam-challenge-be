package com.wezaam.withdrawal.rest;

import com.wezaam.withdrawal.model.User;
import com.wezaam.withdrawal.repository.UserRepository;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Api
@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @GetMapping("/users/{id}")
    public User findById(@PathVariable Long id) {
        return userRepository.findById(id).orElseThrow(
        		() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}

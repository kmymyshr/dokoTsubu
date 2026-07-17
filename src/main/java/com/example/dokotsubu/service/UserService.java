package com.example.dokotsubu.service;

import com.example.dokotsubu.persistence.SpringDataJdbcGateway;
import model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final SpringDataJdbcGateway gateway;
    private final PasswordEncoder passwordEncoder;

    public UserService(SpringDataJdbcGateway gateway, PasswordEncoder passwordEncoder) {
        this.gateway = gateway;
        this.passwordEncoder = passwordEncoder;
    }

    public User findById(int id) {
        return gateway.findUserById(id);
    }

    public User findByName(String name) {
        return gateway.findUserByName(name);
    }

    public User authenticate(String name, String rawPassword) {
        User user = findByName(name);
        if (user == null || !passwordEncoder.matches(rawPassword, user.getPass())) {
            return null;
        }
        return user;
    }

    public boolean register(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPass());
        return gateway.createUser(new User(user.getName(), encodedPassword, user.getBio()));
    }

    public boolean updateBio(int userId, String bio) {
        return gateway.updateUserBio(userId, bio);
    }
}

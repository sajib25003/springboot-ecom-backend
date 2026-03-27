package com.ashik.SpringEcom.controller;

import com.ashik.SpringEcom.model.User;
import com.ashik.SpringEcom.model.enums.Role;
import com.ashik.SpringEcom.repo.UserRepo;
import com.ashik.SpringEcom.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserRepo repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authManager;

    // 1️⃣ Registration
    @PostMapping("/register")
    public String register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER); // default role
        repo.save(user);
        return "User registered!";
    }

    // 2️⃣ Login
    @PostMapping("/login")
    public String login(@RequestBody User user) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
        );
        Optional<User> dbUser = repo.findByEmail(user.getEmail());
        return dbUser.map(value -> jwtUtil.generateToken(value.getEmail())).orElse("Login failed");
    }
}
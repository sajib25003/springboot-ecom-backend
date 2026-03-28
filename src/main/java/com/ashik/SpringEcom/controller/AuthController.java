package com.ashik.SpringEcom.controller;

import com.ashik.SpringEcom.model.ApiResponse;
import com.ashik.SpringEcom.model.Product;
import com.ashik.SpringEcom.model.User;
import com.ashik.SpringEcom.model.enums.Role;
import com.ashik.SpringEcom.repo.UserRepo;
import com.ashik.SpringEcom.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
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
    public ResponseEntity<ApiResponse<User>> register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER); // default role
        if (repo.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        repo.save(user);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "User registered", user)
        );
    }

    // 2️⃣ Login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(@RequestBody User user) {
        try {
            // 1️⃣ Authenticate user
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
            );

            // 2️⃣ Fetch user from DB
            User dbUser = repo.findByEmail(user.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 3️⃣ Remove password before sending
            dbUser.setPassword(null);

            // 4️⃣ Generate JWT token
            String token = jwtUtil.generateToken(dbUser.getEmail());

            // 5️⃣ Set token expiry if your JwtUtil supports it
            long expiryMillis = jwtUtil.getExpiryFromToken(token); // implement this method
            String expiry = Instant.ofEpochMilli(expiryMillis).toString();

            // 6️⃣ Prepare custom response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", dbUser);
            responseData.put("token", token);
            responseData.put("expiry", expiry);

            return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", responseData));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, "Invalid credentials", null));
        }
    }
}
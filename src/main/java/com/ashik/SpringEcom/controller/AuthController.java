package com.ashik.SpringEcom.controller;

import com.ashik.SpringEcom.model.ApiResponse;
import com.ashik.SpringEcom.model.Product;
import com.ashik.SpringEcom.model.RefreshToken;
import com.ashik.SpringEcom.model.User;
import com.ashik.SpringEcom.model.dto.UserResponse;
import com.ashik.SpringEcom.model.enums.Role;
import com.ashik.SpringEcom.repo.RefreshTokenRepo;
import com.ashik.SpringEcom.repo.UserRepo;
import com.ashik.SpringEcom.security.JwtUtil;
import com.ashik.SpringEcom.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserRepo repo;


    @Autowired
    private RefreshTokenRepo refreshRepo;

    @Value("${refresh.expiry}")
    private long REFRESH_EXPIRATION;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private RefreshTokenService refreshTokenService;

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
//            dbUser.setPassword(null);

            UserResponse userResponse = new UserResponse(dbUser);

            // 4️⃣ Generate JWT token
            String accessToken = jwtUtil.generateToken(dbUser.getEmail(), dbUser.getRole().name());
            String refreshTokenStr = jwtUtil.generateRefreshToken(dbUser.getEmail());

            RefreshToken refreshToken = new RefreshToken();
            refreshToken.setToken(refreshTokenStr);
            refreshToken.setEmail(dbUser.getEmail());
            refreshToken.setExpiryDate(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION));

            refreshRepo.save(refreshToken);

            // 5️⃣ Set token expiry if your JwtUtil supports it
            long expiryMillis = jwtUtil.getExpiryFromToken(accessToken); // implement this method
            String expiry = Instant.ofEpochMilli(expiryMillis).toString();

            // find refresh token expiry
            long refreshExpiryMillis = jwtUtil.getExpiryFromToken(refreshTokenStr);
            String refreshExpiry = Instant.ofEpochMilli(refreshExpiryMillis).toString();


            // 6️⃣ Prepare custom response data
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", userResponse);
            responseData.put("accessToken", accessToken);
            responseData.put("refreshToken", refreshToken);
            responseData.put("accessTokenExpiry", expiry);
            responseData.put("refreshTokenExpiry", refreshExpiry);

            return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", responseData));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, "Invalid credentials: " + e.getMessage(), null));
        }
    }


    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String refreshTokenStr = request.get("refreshToken");

        RefreshToken token = refreshRepo.findByToken(refreshTokenStr)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // check if refresh token expired
        if (token.getExpiryDate().before(new Date())) {
            return ResponseEntity.status(401)
                    .body(Map.of("success", false, "message", "Refresh token expired"));
        }

        // get user from email
        User dbUser = repo.findByEmail(token.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateToken(dbUser.getEmail(), dbUser.getRole().name());

        UserResponse userResponse = new UserResponse(dbUser);

        return ResponseEntity.ok(
                Map.of(
                        "accessToken", newAccessToken,
                        "accessTokenExpiry", Instant.ofEpochMilli(jwtUtil.getExpiryFromToken(newAccessToken)).toString(),
                        "user", userResponse
                )
        );
    }

    @PostMapping("/logout-user")
    public ResponseEntity<ApiResponse<Object>> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        refreshTokenService.delete(refreshToken);  // delete from DB
        return ResponseEntity.ok(new ApiResponse<>(true, "Logged out successfully", null));
    }
}
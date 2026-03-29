package com.ashik.SpringEcom.service;

import com.ashik.SpringEcom.model.RefreshToken;
import com.ashik.SpringEcom.repo.RefreshTokenRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepo repo;

    private final long REFRESH_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 days

    // ✅ Save token
    public void save(String token, String email) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(token);
        rt.setEmail(email);
        rt.setExpiryDate(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION));

        repo.save(rt);
    }

    // ✅ Verify token
    public RefreshToken verify(String token) {
        RefreshToken rt = repo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        // check expiry
        if (rt.getExpiryDate().before(new Date())) {
            repo.delete(rt);
            throw new RuntimeException("Refresh token expired");
        }

        return rt;
    }

    // ✅ Delete (logout)
    @Transactional
    public void delete(String token) {
        repo.deleteByToken(token);
    }
}
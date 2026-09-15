package com.airline.security.jwt;

import com.airline.model.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;

public interface JwtService {

    String generateToken(User user);

    String extractUsername(String token);

    Long extractUserId(String token);

    Instant extractExpiration(String token);

    boolean isTokenValid(String token, UserDetails userDetails);

    long getJwtExpiryMs();

}
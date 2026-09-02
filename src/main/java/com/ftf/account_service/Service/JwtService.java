package com.ftf.account_service.Service;

import com.ftf.account_service.Entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiry}")
    private Long expiration;
    public String generateJwtToken(User user)
    {
        SecretKey secretKey = Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(secret));
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email",user.getEmail())
                .claim("role","customer")
                .claim("name",user.getFirstName())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }
}

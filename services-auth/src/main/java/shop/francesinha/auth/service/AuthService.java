package shop.francesinha.auth.service;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;

@Service
public class AuthService {

    @Autowired
    private RSAPrivateKey privateKey;

    public String generateToken(String username) {
        long now = System.currentTimeMillis();
        long jwtExpirationMs = 3600000; //1 hour
        assert !"".equals(username);

        return Jwts.builder()
                .claim("sub", username)       // subject
                .claim("iss", "myapp")         // issuer
                .claim("iat", now / 1000)      // issued at (seconds)
                .claim("exp", (now + jwtExpirationMs) / 1000) // expiration in seconds
                .signWith(privateKey)
                .compact();
    }
}
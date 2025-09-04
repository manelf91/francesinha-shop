package shop.francesinha.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class JwtUtils {

    private final String jwtSecret = "secretKeyExample123secretKeyExample123secretKeyExample123";
    private final long jwtExpirationMs = 3600000; //1 hour
    Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username) {
        SecretKey key = getSecretKey();

        long now = System.currentTimeMillis();
        assert !"".equals(username);
        return Jwts.builder()
                .claim("sub", username)       // subject
                .claim("iss", "myapp")         // issuer
                .claim("iat", now / 1000)      // issued at (seconds)
                .claim("exp", (now + jwtExpirationMs) / 1000) // expiration in seconds
                .signWith(key)                     // no algorithm param needed
                .compact();
    }

    public String getUsernameFromToken(String token) {
        SecretKey key = getSecretKey();
        logger.debug("This is the token {}", token);
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            SecretKey key = getSecretKey();
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch(SecurityException | MalformedJwtException e) {
            throw new AuthenticationCredentialsNotFoundException("JWT was expired or incorrect");
        } catch (ExpiredJwtException e) {
            throw new AuthenticationCredentialsNotFoundException("Expired JWT token.");
        } catch (UnsupportedJwtException e) {
            throw new AuthenticationCredentialsNotFoundException("Unsupported JWT token.");
        } catch (IllegalArgumentException e) {
            throw new AuthenticationCredentialsNotFoundException("JWT token compact of handler are invalid.");
        }
    }
}
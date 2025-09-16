package shop.francesinha.auth.controller;

import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.JWSAlgorithm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import shop.francesinha.auth.service.AuthService;
import shop.francesinha.auth.service.UserService;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RequestMapping("/auth")
@RestController
public class AuthController {

     @Autowired
     private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RSAPublicKey publicKey;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam String username, // Username from the form
            @RequestParam String password // Password from the form
    ) {
        userService.registerUser(username, password);
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestParam String username, // Username from the form
            @RequestParam String password // Password from the form
    ) {
        // Authenticate the user programmatically
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        return ResponseEntity.ok(Map.of(
                "message", "User logged in successfully",
                "token", authService.generateToken(username)));
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> keys() {
        RSAKey jwk = new RSAKey.Builder(publicKey)
                .keyID("auth-key-1")
                .algorithm(JWSAlgorithm.RS256)
                .keyUse(KeyUse.SIGNATURE)
                .build();

        return new JWKSet(jwk).toJSONObject();
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Invalidate the token if necessary
        return ResponseEntity.ok(Map.of("message", "User logged out successfully"));
    }
}
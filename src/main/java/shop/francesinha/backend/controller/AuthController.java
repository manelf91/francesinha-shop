package shop.francesinha.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shop.francesinha.backend.security.ICustomUserDetailsService;
import shop.francesinha.backend.security.JwtUtils;

import java.util.Map;

@RequestMapping("/auth")
@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final ICustomUserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, ICustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam String username, // Username from the form
            @RequestParam String password // Password from the form
    ) {
        userDetailsService.registerUser(username, password);
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
                "token", jwtUtils.generateToken(username)));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Invalidate the token if necessary
        return ResponseEntity.ok(Map.of("message", "User logged out successfully"));
    }
}
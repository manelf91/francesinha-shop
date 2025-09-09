package shop.francesinha.backend.security;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shop.francesinha.backend.exception.UserAlreadyExistAuthenticationException;
import shop.francesinha.backend.model.User;
import shop.francesinha.backend.service.UserService;

import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public CustomUserDetailsService(UserService userService, @Lazy PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // Called by AuthenticationManager when someone tries to log in
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw UsernameNotFoundException.fromUsername(username);
        }

        return org.springframework.security.core.userdetails.User.withUsername(username)
                .password(user.getEncryptedPassword())
                .roles(user.getRoles().toArray(new String[0]))
                .build();
    }

    // Utility to add a new user (from register endpoint)
    public void registerUser(String username, String password) throws UserAlreadyExistAuthenticationException {
        User user = userService.findByUsername(username);
        if (user != null) {
            throw new UserAlreadyExistAuthenticationException("User already exists");
        }
        userService.saveUser(username, passwordEncoder.encode(password));
    }
}
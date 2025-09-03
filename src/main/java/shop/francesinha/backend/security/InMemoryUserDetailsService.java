package shop.francesinha.backend.security;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shop.francesinha.backend.exception.UserAlreadyExistAuthenticationException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryUserDetailsService implements ICustomUserDetailsService {

    private final Map<String, String> registeredUsers = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;

    public InMemoryUserDetailsService(@Lazy PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // Called by AuthenticationManager when someone tries to log in
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String encodedPassword = registeredUsers.get(username);
        if (encodedPassword == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return User.withUsername(username)
                .password(encodedPassword)
                .roles("USER")
                .build();
    }

    // Utility to add a new user (from register endpoint)
    @Override
    public void registerUser(String username, String password) throws UserAlreadyExistAuthenticationException {
        if (registeredUsers.containsKey(username)) {
            throw new UserAlreadyExistAuthenticationException("User already exists");
        }
        registeredUsers.put(username, passwordEncoder.encode(password));
    }
}
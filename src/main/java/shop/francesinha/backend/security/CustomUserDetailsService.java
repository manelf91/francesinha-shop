package shop.francesinha.backend.security;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shop.francesinha.backend.exception.UserAlreadyExistAuthenticationException;
import shop.francesinha.backend.model.User;
import shop.francesinha.backend.repo.IUserRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CustomUserDetailsService implements ICustomUserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final IUserRepository userRepository;

    public CustomUserDetailsService(IUserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Called by AuthenticationManager when someone tries to log in
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return org.springframework.security.core.userdetails.User.withUsername(username)
                .password(user.getEncryptedPassword())
                .roles("USER")
                .build();
    }

    // Utility to add a new user (from register endpoint)
    @Override
    public void registerUser(String username, String password, String... roles) throws UserAlreadyExistAuthenticationException {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            throw new UserAlreadyExistAuthenticationException("User already exists");
        }
        userRepository.save(username, passwordEncoder.encode(password), roles);
    }

    @Override
    public void deleteUser(String username) {
        userRepository.deleteByUsername(username);
    }
}
package shop.francesinha.backend.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import shop.francesinha.backend.exception.UserAlreadyExistAuthenticationException;
import shop.francesinha.backend.model.User;
import shop.francesinha.backend.service.UserService;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
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
        userService.saveUser(username, password);
    }
}
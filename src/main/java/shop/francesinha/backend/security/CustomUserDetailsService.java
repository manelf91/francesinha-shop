package shop.francesinha.backend.security;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shop.francesinha.backend.exception.UserAlreadyExistAuthenticationException;
import shop.francesinha.backend.model.User;
import shop.francesinha.backend.repo.IUserRepository;

import java.util.List;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

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
                .roles(user.getRoles().toArray(new String[0]))
                .build();
    }

    // Utility to add a new user (from register endpoint)
    public void registerUser(String username, String password, String ...roles) throws UserAlreadyExistAuthenticationException {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            throw new UserAlreadyExistAuthenticationException("User already exists");
        }
        userRepository.save(username, passwordEncoder.encode(password), Set.of(roles));
    }

    public void addRoleToUser(String username, String role) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        Set<String> roles = user.getRoles();
        if (!roles.contains(role)) {
            Set<String> newRoles = new java.util.HashSet<>(roles);
            newRoles.add(role);
            user.setRoles(newRoles);
            userRepository.update(user);
        }
    }

    public List<User> getUsersByRole(String admin) {
        return userRepository.findByRolesContains(admin);
    }
}
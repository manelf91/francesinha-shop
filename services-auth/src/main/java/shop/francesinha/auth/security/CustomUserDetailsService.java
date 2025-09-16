package shop.francesinha.auth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import shop.francesinha.auth.model.User;
import shop.francesinha.auth.service.UserService;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

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
}
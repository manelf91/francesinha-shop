package shop.francesinha.backend.security;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface ICustomUserDetailsService extends UserDetailsService {
    public void registerUser(String username, String password);
}

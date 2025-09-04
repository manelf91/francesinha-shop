package shop.francesinha.backend.security;

import org.springframework.security.core.userdetails.UserDetailsService;
import shop.francesinha.backend.exception.UserAlreadyExistAuthenticationException;

public interface ICustomUserDetailsService extends UserDetailsService {
    public void registerUser(String username, String password, String... roles) throws UserAlreadyExistAuthenticationException;

    public void deleteUser(String username);
}

package shop.francesinha.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import shop.francesinha.backend.model.User;
import shop.francesinha.backend.repo.UserRepository;

import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findByRole(String role) {
        return userRepository.findByRolesContains(role);
    }

    public int countByRole(String role) {
        return userRepository.countByRolesContains(role);
    }

    public void saveUser(String username, String encode) {
        User user = new User();
        user.setUsername(username);
        user.setEncryptedPassword(encode);
        user.setRoles(Set.of("USER"));
        userRepository.save(user);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public User updateUser(User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser == null) {
            throw UsernameNotFoundException.fromUsername(user.getUsername());
        }

        long adminCount = userRepository.countByRolesContains("ADMIN");
        boolean wasAdmin = existingUser.getRoles().contains("ADMIN");
        boolean isAdminNow = user.getRoles().contains("ADMIN");

        if (wasAdmin && !isAdminNow && adminCount <= 1) {
            throw new DataIntegrityViolationException("Cannot demote last admin");
        }

        return userRepository.save(user);
    }

    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw UsernameNotFoundException.fromUsername(username);
        }

        long adminCount = userRepository.countByRolesContains("ADMIN");
        if (user.getRoles().contains("ADMIN") && adminCount <= 1) {
            throw new DataIntegrityViolationException("Cannot remove last admin");
        }

        userRepository.delete(user);
    }

    public User getDefaultAdminUser() {
        return new User("admin", "1234", Set.of("ADMIN", "USER"));
    }
}

package shop.francesinha.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import shop.francesinha.backend.model.User;
import shop.francesinha.backend.repo.IUserRepository;

@Service
public class UserService {

    @Autowired
    private IUserRepository userRepository;

    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User with username " + username + " not found.");
        }

        long adminCount = userRepository.countByRolesContains("ADMIN");
        if (user.getRoles().contains("ADMIN") && adminCount <= 1) {
            throw new DataIntegrityViolationException("Cannot remove last admin");
        }

        userRepository.delete(user);
    }

    public User updateUser(User user) {
        User existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser == null) {
            throw new RuntimeException("User with username " + user.getUsername() + " not found.");
        }

        long adminCount = userRepository.countByRolesContains("ADMIN");
        boolean wasAdmin = existingUser.getRoles().contains("ADMIN");
        boolean isAdminNow = user.getRoles().contains("ADMIN");

        if (wasAdmin && !isAdminNow && adminCount <= 1) {
            throw new DataIntegrityViolationException("Cannot demote last admin");
        }

        return userRepository.update(user);
    }
}

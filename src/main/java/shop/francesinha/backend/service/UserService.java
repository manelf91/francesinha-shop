package shop.francesinha.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import shop.francesinha.backend.dto.UserDTO;
import shop.francesinha.backend.dto.UserMapper;
import shop.francesinha.backend.model.User;
import shop.francesinha.backend.repo.UserRepository;

import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService (@Lazy PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

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

    public void saveUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEncryptedPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of("USER"));
        userRepository.save(user);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void updateUser(UserDTO userDTO) {
        User existingUser = userRepository.findByUsername(userDTO.getUsername());
        if (existingUser == null) {
            throw UsernameNotFoundException.fromUsername(userDTO.getUsername());
        }

        long adminCount = userRepository.countByRolesContains("ADMIN");
        boolean wasAdmin = existingUser.getRoles().contains("ADMIN");
        boolean isAdminNow = userDTO.getRoles().contains("ADMIN");

        if (wasAdmin && !isAdminNow && adminCount <= 1) {
            throw new DataIntegrityViolationException("Cannot demote last admin");
        }

        User user = UserMapper.INSTANCE.toEntity(userDTO);
        user.setId(existingUser.getId()); // Ensure update, not insert
        user.setEncryptedPassword(passwordEncoder.encode(userDTO.getPassword()));

        userRepository.save(user);
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

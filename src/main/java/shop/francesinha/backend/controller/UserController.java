package shop.francesinha.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import shop.francesinha.backend.model.User;
import shop.francesinha.backend.repo.IUserRepository;
import shop.francesinha.backend.service.UserService;

import java.util.List;
import java.util.Map;

@RequestMapping("/user")
@RestController
public class UserController {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @DeleteMapping("/{username}")
    public Map<String, String> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return Map.of("message", "User deleted successfully");
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        return userService.updateUser(user);
    }
}

package shop.francesinha.auth.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import shop.francesinha.auth.dto.UserDTO;
import shop.francesinha.auth.model.User;
import shop.francesinha.auth.service.UserService;

import java.util.List;
import java.util.Map;

@RequestMapping("/user")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @PutMapping
    public void updateUser(@Valid @RequestBody UserDTO user) {
        userService.updateUser(user);
    }

    @DeleteMapping("/{username}")
    public Map<String, String> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return Map.of("message", "User deleted successfully");
    }
}

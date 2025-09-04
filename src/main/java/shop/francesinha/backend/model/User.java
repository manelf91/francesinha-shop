package shop.francesinha.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    @NotBlank(message = "Username must not be empty")
    private String username;

    @NotBlank(message = "Password must not be empty")
    private String encryptedPassword;

    private List<String> roles;

    public User(String username, String password, String[] roles) {
        this.username = username;
        this.encryptedPassword = password;
        this.roles = List.of(roles);
    }
}

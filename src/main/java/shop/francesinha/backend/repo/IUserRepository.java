package shop.francesinha.backend.repo;

import shop.francesinha.backend.model.User;

import java.util.List;
import java.util.Set;

public interface IUserRepository {

    public void delete(User user);

    List<User> findAll();

    public User findByUsername(String username);

    public User save(String username, String encryptedPassword, Set<String> roles);

    public User update(User user);

    public int countByRolesContains(String role);

    List<User> findByRolesContains(String role);
}

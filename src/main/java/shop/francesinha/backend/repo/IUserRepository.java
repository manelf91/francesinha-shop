package shop.francesinha.backend.repo;

import shop.francesinha.backend.model.User;

import java.util.List;

public interface IUserRepository {

    public void deleteByUsername(String username);

    List<User> findAll();

    public User findByUsername(String username);

    public User save(String username, String encryptedPassword, String[] roles);

    public User update(User user);
}

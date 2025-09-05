package shop.francesinha.backend.repo;

import org.springframework.stereotype.Repository;
import shop.francesinha.backend.model.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements IUserRepository {

    private final Map<String, User> registeredUsers = new ConcurrentHashMap<>();

    @Override
    public void deleteByUsername(String username) {
        registeredUsers.remove(username);
    }

    @Override
    public List<User> findAll() {
        return registeredUsers.values().stream().toList();
    }

    @Override
    public User findByUsername(String username) {
        return registeredUsers.getOrDefault(username, null);
    }

    @Override
    public User save(String username, String encryptedPassword, String[] roles) {
        User user = new User(username, encryptedPassword, roles);
        registeredUsers.put(username, user);
        return user;
    }

    @Override
    public User update(User user) {
        registeredUsers.put(user.getUsername(), user);
        return user;
    }
}
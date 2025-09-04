package shop.francesinha.backend.repo;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import shop.francesinha.backend.model.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements IUserRepository {

    private final Map<String, String> registeredUsers = new ConcurrentHashMap<>();

    @Override
    public void deleteByUsername(String username) {
        registeredUsers.remove(username);
    }

    @Override
    public List<User> findAll() {
        return registeredUsers.keySet().stream()
                .map(username -> new User(username, registeredUsers.get(username), new String[]{"USER"}))
                .toList();
    }

    @Override
    public User findByUsername(String username) {
        return registeredUsers.containsKey(username) ?
                new User(username, registeredUsers.get(username), new String[]{"USER"}) : null;
    }

    @Override
    public User save(String username, String encryptedPassword, String[] roles) {
        registeredUsers.put(username, encryptedPassword);
        return new User(username, encryptedPassword, roles);
    }
}
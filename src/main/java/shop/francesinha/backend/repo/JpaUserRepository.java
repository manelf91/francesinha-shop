package shop.francesinha.backend.repo;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import shop.francesinha.backend.model.User;

@Primary
@Repository
public interface JpaUserRepository extends JpaRepository<User, Long>, IUserRepository {

    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.username = :username")
    public void deleteByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.username = :username")
    public User findByUsername(@NotBlank String username);

    @Override
    public default User save(String username, String encryptedPassword, String[] roles) {
        User user = new User(username, encryptedPassword, roles);
        return this.save(user);
    }
}

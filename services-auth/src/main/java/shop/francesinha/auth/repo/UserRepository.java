package shop.francesinha.auth.repo;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import shop.francesinha.auth.model.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.username = :username")
    public User findByUsername(@NotBlank String username);

    @Query("SELECT COUNT(u) FROM User u WHERE :admin MEMBER OF u.roles")
    public int countByRolesContains(String admin);

    @Query("SELECT u FROM User u WHERE :role MEMBER OF u.roles")
    public List<User> findByRolesContains(String role);
}

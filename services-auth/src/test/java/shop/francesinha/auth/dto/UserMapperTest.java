package shop.francesinha.auth.dto;

import org.junit.jupiter.api.Test;
import shop.francesinha.auth.dto.UserDTO;
import shop.francesinha.auth.dto.UserMapper;
import shop.francesinha.auth.model.User;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    @Test
    void testToEntity() {
        UserDTO dto = new UserDTO();
        dto.setUsername("john");
        dto.setPassword("pass123");
        dto.setRoles(Set.of("USER", "ADMIN"));

        User entity = UserMapper.INSTANCE.toEntity(dto);

        assertEquals(dto.getUsername(), entity.getUsername());
        assertEquals(dto.getRoles(), entity.getRoles());
    }

    @Test
    void testToDTO() {
        User entity = new User();
        entity.setUsername("jane");
        entity.setEncryptedPassword("secret");
        entity.setRoles(Set.of("USER"));

        UserDTO dto = UserMapper.INSTANCE.toDTO(entity);

        assertEquals(entity.getUsername(), dto.getUsername());
        assertEquals(entity.getRoles(), dto.getRoles());
    }
}

package shop.francesinha.backend.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shop.francesinha.backend.common.TestUtils;
import shop.francesinha.backend.dto.UserDTO;
import shop.francesinha.backend.dto.UserMapper;
import shop.francesinha.backend.model.User;
import shop.francesinha.backend.repo.UserRepository;
import shop.francesinha.backend.service.UserService;

import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoSpyBean
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        Mockito.clearInvocations(userRepository);
    }

    @Test
    public void shouldUpdateUserToAdminRole() throws Exception {
        String username = "userToUpdate";
        String password = "password";
        Set<String> roles = Set.of("USER");
        User userToUpdate = userService.saveUser(new User(username, password, roles));
        Mockito.clearInvocations(userRepository); // reset invocation count

        Set<String> newRoles = Set.of("ADMIN", "USER");
        UserDTO userDTO = new UserDTO(username, password, newRoles);

        // Update user details
        TestUtils.putEndpoint(mockMvc, "/user", userDTO).andExpect(status().isOk());

        Mockito.verify(userRepository).save(Mockito.argThat(u ->
                u.getId().equals(userToUpdate.getId())
                && u.getUsername().equals(username)
                && u.getRoles().equals(newRoles)));

        User updatedUser = userService.findByUsername(username);
        assert updatedUser != null;
        assert updatedUser.getRoles().equals(newRoles);
    }

    @Test
    public void shouldUpdateAdminToUserRole() throws Exception {
        String username = "adminToUpdate";
        String password = "password";
        Set<String> roles = Set.of("ADMIN", "USER");
        User adminToUpdate = userService.saveUser(new User(username, password, roles));
        Mockito.clearInvocations(userRepository); // reset invocation count

        Set<String> newRoles = Set.of("USER");
        UserDTO userDTO = new UserDTO(username, password, newRoles);

        // Update user details
        TestUtils.putEndpoint(mockMvc, "/user", userDTO).andExpect(status().isOk());

        Mockito.verify(userRepository).save(Mockito.argThat(u ->
                u.getId().equals(adminToUpdate.getId())
                        && u.getUsername().equals(username)
                        && u.getRoles().equals(newRoles)));

        User updatedUser = userService.findByUsername(username);
        assert updatedUser != null;
        assert updatedUser.getRoles().equals(newRoles);
    }

    @Test
    public void shouldUpdateUserPassword() throws Exception {
        String username = "userToChangePassword";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        Set<String> roles = Set.of("USER");
        User userToUpdate = userService.saveUser(new User(username, oldPassword, roles));
        Mockito.clearInvocations(userRepository); // reset invocation count

        UserDTO userDTO = new UserDTO(username, newPassword, roles);

        // Update user details
        TestUtils.putEndpoint(mockMvc, "/user", userDTO).andExpect(status().isOk());

        Mockito.verify(userRepository).save(Mockito.argThat(u ->
                u.getId().equals(userToUpdate.getId())
                && u.getUsername().equals(username)
                && passwordEncoder.matches(newPassword, u.getEncryptedPassword())
                && u.getRoles().equals(roles)));

        User updatedUser = userService.findByUsername(username);
        assert updatedUser != null;
        assert passwordEncoder.matches(newPassword, updatedUser.getEncryptedPassword());
    }

    @Test
    public void shouldNotUpdateNotExistentUser() throws Exception {
        UserDTO nonExistentUser = new UserDTO("nonExistentUser", "password", Set.of("USER"));
        TestUtils.putEndpoint(mockMvc, "/user", nonExistentUser)
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"message\":\"user not found\"}"));
    }

    @Test
    void shouldNotDeleteNotExistentUser() throws Exception {
        String nonExistentUsername = "nonExistentUser";
        TestUtils.deleteEndpoint(mockMvc, "/user/" + nonExistentUsername)
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"message\":\"user not found\"}"));
    }

    @Test
    public void shouldNotDeleteLastAdmin() throws Exception {
        List<User> admins = userService.findByRole("ADMIN"); //this should return at least the admin created at startup
        assert !admins.isEmpty();

        for (int i = 0; i < admins.size(); i++) {
            User admin = admins.get(i);
            ResultActions resultActions = TestUtils.deleteEndpoint(mockMvc, "/user/" + admin.getUsername());
            if (i == admins.size() - 1) {
                resultActions.andExpect(status().isBadRequest())
                        .andExpect(content().json("{\"message\":\"Cannot remove last admin\"}"));
            } else {
                resultActions.andExpect(status().isOk())
                        .andExpect(content().json("{\"message\":\"User deleted successfully\"}"));
            }
        }

        Mockito.verify(userRepository, Mockito.times(admins.size() - 1)).delete(Mockito.any(User.class));
    }

    @Test
    public void shouldNotRemoveLastAdminRole() throws Exception {
        List<User> admins = userService.findByRole("ADMIN"); //this should return at least the admin created at startup
        assert !admins.isEmpty();

        for (int i = 0; i < admins.size(); i++) {
            User admin = admins.get(i);
            UserDTO adminDto = UserMapper.INSTANCE.toDTO(admin);
            adminDto.setRoles(Set.of("USER")); // Attempt to remove ADMIN role
            adminDto.setPassword("dummyPassword"); // set password to ensure update
            ResultActions resultActions = TestUtils.putEndpoint(mockMvc, "/user", adminDto);

            if (i == admins.size() - 1) {
                resultActions.andExpect(status().isBadRequest())
                        .andExpect(content().json("{\"message\":\"Cannot demote last admin\"}"));
            } else {
                resultActions.andExpect(status().isOk());
                // Verify that the user now has only USER role
                User updatedUser = userService.findByUsername(admin.getUsername());
                assert updatedUser != null;
                assert updatedUser.getRoles().equals(Set.of("USER"));
            }
        }

        Mockito.verify(userRepository, Mockito.times(admins.size() - 1)).save(Mockito.any(User.class));
    }

    @Test
    public void shouldDeleteAdminUser() throws Exception {
        String username = "adminToDelete";
        String password = "password";
        Set<String> roles = Set.of("ADMIN", "USER");
        User adminToDelete = new User(username, password, roles);
        userService.saveUser(adminToDelete);

        TestUtils.deleteEndpoint(mockMvc, "/user/" + username)
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"User deleted successfully\"}"));

        Mockito.verify(userRepository).delete(Mockito.argThat(u -> u.getUsername().equals(username)));
    }

    @Test
    public void shouldDeleteUser() throws Exception {
        String username = "userToDelete";
        String password = "password";
        Set<String> roles = Set.of("USER");
        User userToDelete = new User(username, password, roles);
        userService.saveUser(userToDelete);

        TestUtils.deleteEndpoint(mockMvc, "/user/" + username)
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"User deleted successfully\"}"));

        Mockito.verify(userRepository).delete(Mockito.argThat(u -> u.getUsername().equals(username)));
        assert userService.findByUsername(username) == null;
    }
}
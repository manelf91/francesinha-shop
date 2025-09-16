package shop.francesinha.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import shop.francesinha.auth.dto.UserDTO;
import shop.francesinha.auth.dto.UserMapper;
import shop.francesinha.auth.exception.UserAlreadyExistAuthenticationException;
import shop.francesinha.auth.model.User;
import shop.francesinha.auth.repo.UserRepository;
import shop.francesinha.auth.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void findByUsername_shouldReturnUser() {
        User user = new User();
        user.setUsername("john");
        when(userRepository.findByUsername("john")).thenReturn(user);

        User result = userService.findByUsername("john");
        assertEquals("john", result.getUsername());
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        User user1 = new User();
        User user2 = new User();
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void countByRole_shouldReturnCount() {
        when(userRepository.countByRolesContains("ADMIN")).thenReturn(3);
        int count = userService.countByRole("ADMIN");
        assertEquals(3, count);
    }

    @Test
    void saveUser_shouldSaveNewUser() {
        UserDTO dto = new UserDTO("john", "pass", Set.of("USER"));
        when(userRepository.findByUsername("john")).thenReturn(null);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");

        UserDTO result = userService.saveUser(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("john", captor.getValue().getUsername());
        assertEquals("encodedPass", captor.getValue().getEncryptedPassword());
        assertEquals(dto, result);
    }

    @Test
    void saveUser_shouldThrowIfUserExists() {
        UserDTO dto = new UserDTO("john", "pass", Set.of("USER"));
        User existing = new User();
        when(userRepository.findByUsername("john")).thenReturn(existing);

        assertThrows(UserAlreadyExistAuthenticationException.class, () -> userService.saveUser(dto));
    }

    @Test
    void updateUser_shouldUpdateUser() {
        UserDTO dto = new UserDTO("john", "newpass", Set.of("USER"));
        User existing = new User();
        existing.setUsername("john");
        existing.setId(1L);
        existing.setRoles(Set.of("USER", "ADMIN"));
        when(userRepository.findByUsername("john")).thenReturn(existing);
        when(userRepository.countByRolesContains("ADMIN")).thenReturn(2);
        when(passwordEncoder.encode("newpass")).thenReturn("encodedNewPass");

        userService.updateUser(dto);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals("encodedNewPass", captor.getValue().getEncryptedPassword());
    }

    @Test
    void updateUser_shouldThrowIfUserNotFound() {
        UserDTO dto = new UserDTO("john", "pass", Set.of("USER"));
        when(userRepository.findByUsername("john")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> userService.updateUser(dto));
    }

    @Test
    void updateUser_shouldThrowIfDemotingLastAdmin() {
        UserDTO dto = new UserDTO("john", "pass", Set.of("USER"));
        User existing = new User();
        existing.setUsername("john");
        existing.setId(1L);
        existing.setRoles(Set.of("ADMIN"));
        when(userRepository.findByUsername("john")).thenReturn(existing);
        when(userRepository.countByRolesContains("ADMIN")).thenReturn(1);

        assertThrows(DataIntegrityViolationException.class, () -> userService.updateUser(dto));
    }

    @Test
    void deleteUser_shouldDeleteUser() {
        User user = new User();
        user.setUsername("john");
        user.setRoles(Set.of("USER"));
        when(userRepository.findByUsername("john")).thenReturn(user);
        when(userRepository.countByRolesContains("ADMIN")).thenReturn(2);

        userService.deleteUser("john");
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_shouldThrowIfUserNotFound() {
        when(userRepository.findByUsername("john")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class, () -> userService.deleteUser("john"));
    }

    @Test
    void deleteUser_shouldThrowIfDeletingLastAdmin() {
        User user = new User();
        user.setUsername("john");
        user.setRoles(Set.of("ADMIN"));
        when(userRepository.findByUsername("john")).thenReturn(user);
        when(userRepository.countByRolesContains("ADMIN")).thenReturn(1);

        assertThrows(DataIntegrityViolationException.class, () -> userService.deleteUser("john"));
    }

    @Test
    void getDefaultAdminUser_shouldReturnDefaultAdmin() {
        UserDTO dto = userService.getDefaultAdminUser();
        assertEquals("admin", dto.getUsername());
        assertEquals("1234", dto.getPassword());
        assertTrue(dto.getRoles().contains("ADMIN"));
        assertTrue(dto.getRoles().contains("USER"));
    }

    @Test
    void registerUser_shouldSaveUserWithUserRole() {
        UserDTO dto = new UserDTO("john", "pass", Set.of("USER"));
        when(userRepository.findByUsername("john")).thenReturn(null);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");

        userService.registerUser("john", "pass");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("john", captor.getValue().getUsername());
        assertEquals("encodedPass", captor.getValue().getEncryptedPassword());
        assertTrue(captor.getValue().getRoles().contains("USER"));
    }
}

package shop.francesinha.backend.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import shop.francesinha.backend.common.TestUtils;
import shop.francesinha.backend.dto.UserDTO;
import shop.francesinha.backend.model.User;
import shop.francesinha.backend.security.JwtAuthenticationFilter;
import shop.francesinha.backend.service.UserService;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    public void shouldUpdateUser() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testUser");
        userDTO.setRoles(Set.of("USER"));
        userDTO.setPassword("newPassword");

        Mockito.doNothing().when(userService).updateUser(Mockito.any(UserDTO.class));

        TestUtils.putEndpoint(mockMvc, "/user", userDTO)
                .andExpect(status().isOk());
    }

    @Test
    public void shouldGetAllUsers() throws Exception {
        User user1 = new User();
        user1.setUsername("user1");

        User user2 = new User();
        user2.setUsername("user2");
        List<User> users = List.of(user1, user2);

        when(userService.findAll()).thenReturn(users);
        TestUtils.getEndpoint(mockMvc, "/user")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].username").value("user2"));
    }

    @Test
    public void shouldDeleteUser() throws Exception {
        String username = "user";
        Mockito.doNothing().when(userService).deleteUser(username);

        TestUtils.deleteEndpoint(mockMvc, "/user/" + username)
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"User deleted successfully\"}"));
    }
}
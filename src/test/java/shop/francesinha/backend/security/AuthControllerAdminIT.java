package shop.francesinha.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import shop.francesinha.backend.common.TestUtils;
import shop.francesinha.backend.model.User;
import shop.francesinha.backend.service.UserService;

import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerAdminIT {

    private static int userCount = 0;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Test
    public void ShouldNotAccessAdminEndpointWithoutAuth() throws Exception {
        String username = "user" + userCount++;
        String password = "password";

        // Register user with ADMIN role
        TestUtils.registerUser(mockMvc, username, password)
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"User registered successfully\"}"));

        // Log in the user
        String loginResult = TestUtils.loginUser(mockMvc, username, password)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract token from response
        String token = loginResult.split("\"token\":\"")[1].split("\"")[0];

        // Access admin endpoint with token
        TestUtils.getEndpointWithToken(mockMvc, "/user", token)
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldAccessAdminEndpointWithAdminRole() throws Exception {
        String username = "adminUser" + userCount++;
        String password = "adminpassword";

        // Register user with ADMIN role
        TestUtils.registerUser(mockMvc, username, password)
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"User registered successfully\"}"));

        // Assign ADMIN role to the user
        User user = userService.findByUsername(username);
        user.setRoles(Set.of("USER", "ADMIN"));
        userService.saveUser(user);

        // Log in the user
        String loginResult = TestUtils.loginUser(mockMvc, username, password)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract token from response
        String token = loginResult.split("\"token\":\"")[1].split("\"")[0];

        // Access admin endpoint
        TestUtils.getEndpointWithToken(mockMvc, "/user", token)
                .andExpect(status().isOk());
    }
}

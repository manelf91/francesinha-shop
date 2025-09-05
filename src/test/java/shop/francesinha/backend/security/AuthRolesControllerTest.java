package shop.francesinha.backend.security;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shop.francesinha.backend.controller.AuthController;
import shop.francesinha.backend.controller.UserController;


import static java.time.LocalTime.now;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, UserController.class})
@ComponentScan(basePackages = "shop.francesinha.backend.security, shop.francesinha.backend.repo")
public class AuthRolesControllerTest extends AbstractAuthControllerTest {

    private static int userCount = 0;

    @MockitoSpyBean
    private CustomUserDetailsService userDetailsService;

    @Test
    public void ShouldNotAccessAdminEndpointWithoutAuth() throws Exception {
        String username = "adminUser" + userCount++;
        String password = "adminpassword";

        // Register user with ADMIN role
        registerUser(username, password)
            .andExpect(status().isOk())
            .andExpect(content().json("{\"message\":\"User registered successfully\"}"));

        // Log in the user
        String loginResult = loginUser(username, password)
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        // Extract token from response
        String token = loginResult.split("\"token\":\"")[1].split("\"")[0];

        // Access admin endpoint with token
        getEndpointWithToken("/user", token)
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldAccessAdminEndpointWithAdminRole() throws Exception {
        String username = "adminUser" + userCount++;
        String password = "adminpassword";

        // Register user with ADMIN role
        registerUser(username, password)
            .andExpect(status().isOk())
            .andExpect(content().json("{\"message\":\"User registered successfully\"}"));

        // Assign ADMIN role to the user
        userDetailsService.addRoleToUser(username, "ADMIN");

        // Log in the user
        String loginResult = loginUser(username, password)
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        // Extract token from response
        String token = loginResult.split("\"token\":\"")[1].split("\"")[0];

        // Access admin endpoint
        getEndpointWithToken("/user", token)
            .andExpect(status().isOk());
    }
}
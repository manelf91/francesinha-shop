package shop.francesinha.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.servlet.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import shop.francesinha.backend.controller.ProductController;
import shop.francesinha.backend.model.Product;
import shop.francesinha.backend.repo.ProductRepository;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@ComponentScan(basePackages = "shop.francesinha.backend.security")
public class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoSpyBean
    private ICustomUserDetailsService userDetailsService;

    @Test
    public void testRegisterUser() throws Exception {
        String username = "testRegisterUser";
        String password = "testpassword";

        mockMvc.perform(post("/auth/register")
                        .with(csrf().asHeader()) // optional if CSRF disabled
                        .param("username", username)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"User registered successfully\"}"));
    }

    @Test
    public void testLoginUser() throws Exception {
        String username = "testLoginUser";
        String password = "testpassword";

        // First, register the user
        mockMvc.perform(post("/auth/register")
                        .with(csrf().asHeader()) // optional if CSRF disabled
                        .param("username", username)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        // Then, attempt to log in
        String response = mockMvc.perform(post("/auth/login")
                        .with(csrf().asHeader()) // optional if CSRF disabled
                        .param("username", username)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        //Check message
        assertTrue(response.contains("User logged in successfully"));

        // Extract token from response
        String token = Objects.requireNonNull(response.split("\"token\":\"")[1]).split("\"")[0];
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Verify that the userDetailsService's registerUser method was called
        Mockito.verify(userDetailsService, Mockito.times(1)).registerUser(username, password);
        Mockito.verify(userDetailsService, Mockito.times(1)).loadUserByUsername(username);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertNotNull(userDetails.getPassword());
        assertFalse(userDetails.getPassword().isEmpty());
    }

    @Test
    public void testCallWithToken() throws Exception {
        String username = "testCallWithToken";
        String password = "testpassword";

        // First, register the user
        mockMvc.perform(post("/auth/register")
                        .with(csrf().asHeader()) // optional if CSRF disabled
                        .param("username", username)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        // Then, attempt to log in
        String response = mockMvc.perform(post("/auth/login")
                        .with(csrf().asHeader()) // optional if CSRF disabled
                        .param("username", username)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        //Check message
        assertTrue(response.contains("User logged in successfully"));

        // Extract token from response
        String token = Objects.requireNonNull(response.split("\"token\":\"")[1]).split("\"")[0];
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // Now, call a secured endpoint with the token
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"message\":\"User logged out successfully\"}"));
    }

    @Test
    public void testCallWithoutToken() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testLoginUser_WrongPassword() throws Exception {
        String username = "testLoginUser_WrongPassword";
        String password = "testpassword";

        // First, register the user
        mockMvc.perform(post("/auth/register")
                        .with(csrf().asHeader()) // optional if CSRF disabled
                        .param("username", username)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        // Then, attempt to log in with wrong password
        mockMvc.perform(post("/auth/login")
                        .with(csrf().asHeader()) // optional if CSRF disabled
                        .param("username", username)
                        .param("password", "wrongpassword")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"message\":\"Invalid username or password\"}"));

        // Verify that the userDetailsService's registerUser method was called
        Mockito.verify(userDetailsService, Mockito.times(1)).registerUser(username, password);
        Mockito.verify(userDetailsService, Mockito.atLeastOnce()).loadUserByUsername(username);
    }

    @Test
    public void testRegisterUser_UserAlreadyExists() throws Exception {
        String username = "testRegisterUser_UserAlreadyExists";
        String password = "testpassword";

        // First, register the user
        mockMvc.perform(post("/auth/register")
                        .with(csrf().asHeader()) // optional if CSRF disabled
                        .param("username", username)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk());

        // Attempt to register the same user again
        mockMvc.perform(post("/auth/register")
                        .with(csrf().asHeader()) // optional if CSRF disabled
                        .param("username", username)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"message\":\"User already exists\"}"));

        // Verify that the userDetailsService's registerUser method was called twice
        Mockito.verify(userDetailsService, Mockito.times(2)).registerUser(username, password);
    }

    @Test
    public void testLoginUser_NonExistentUser() throws Exception {
        String username = "nonExistentUser";
        String password = "somepassword";

        // Attempt to log in with a non-existent user
        mockMvc.perform(post("/auth/login")
                        .with(csrf().asHeader()) // optional if CSRF disabled
                        .param("username", username)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isUnauthorized())
                .andExpect(content().json("{\"message\":\"Invalid username or password\"}"));

        // Verify that the userDetailsService's loadUserByUsername method was called
        Mockito.verify(userDetailsService, Mockito.atLeastOnce()).loadUserByUsername(username);
    }
}

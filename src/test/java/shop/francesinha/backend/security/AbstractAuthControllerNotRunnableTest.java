package shop.francesinha.backend.security;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import shop.francesinha.backend.common.TestUtils;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AbstractAuthControllerNotRunnableTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoSpyBean
    protected CustomUserDetailsService userDetailsService;

    @Test
    public void testRegisterUser() throws Exception {
        String username = "testRegisterUser";
        String password = "testpassword";

        TestUtils.registerUser(mockMvc, username, password)
            .andExpect(status().isOk())
            .andExpect(content().json("{\"message\":\"User registered successfully\"}"));
    }

    @Test
    public void testLoginUser() throws Exception {
        String username = "testLoginUser";
        String password = "testpassword";

        // First, register the user
        TestUtils.registerUser(mockMvc, username, password).andExpect(status().isOk());

        // Then, attempt to log in
        String loginResult = TestUtils.loginUser(mockMvc, username, password)
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        //Check message
        assertTrue(loginResult.contains("User logged in successfully"));

        // Extract token from response
        String token = Objects.requireNonNull(loginResult.split("\"token\":\"")[1]).split("\"")[0];
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
        TestUtils.registerUser(mockMvc, username, password).andExpect(status().isOk());

        // Then, attempt to log in
        String loginResult = TestUtils.loginUser(mockMvc, username, password)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // Extract token from response
        String token = Objects.requireNonNull(loginResult.split("\"token\":\"")[1]).split("\"")[0];

        // Now, call a secured endpoint with the token
        TestUtils.postEndpointWithToken(mockMvc, "/auth/logout", token)
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
        String wrongPassword = "wrongpassword";

        // First, register the user
        TestUtils.registerUser(mockMvc, username, password).andExpect(status().isOk());

        // Then, attempt to log in with wrong password
        TestUtils.loginUser(mockMvc, username, wrongPassword)
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
        TestUtils.registerUser(mockMvc, username, password).andExpect(status().isOk());

        // Attempt to register the same user again
        TestUtils.registerUser(mockMvc, username, password)
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
        TestUtils.loginUser(mockMvc, username, password)
            .andExpect(status().isUnauthorized())
            .andExpect(content().json("{\"message\":\"Invalid username or password\"}"));

        // Verify that the userDetailsService's loadUserByUsername method was called
        Mockito.verify(userDetailsService, Mockito.atLeastOnce()).loadUserByUsername(username);
    }

    @Test
    public void testRegisterUser_MissingParameters() throws Exception {
        // Missing username
        TestUtils.registerUser(mockMvc, null, "somepassword").andExpect(status().isBadRequest());

        // Missing password
        TestUtils.registerUser(mockMvc, "someuser", null).andExpect(status().isBadRequest());
    }

    @Test
    public void testLoginUser_MissingParameters() throws Exception {
        // Missing username
        TestUtils.loginUser(mockMvc, null, "somepassword").andExpect(status().isBadRequest());

        // Missing password
        TestUtils.loginUser(mockMvc, "someuser", null).andExpect(status().isBadRequest());
    }

    @Test
    public void testLogoutUser_MissingToken() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .with(csrf().asHeader())) // optional if CSRF disabled
                .andExpect(status().isForbidden());
    }
}

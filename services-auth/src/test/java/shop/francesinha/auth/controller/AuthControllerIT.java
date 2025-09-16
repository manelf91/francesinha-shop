package shop.francesinha.auth.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import shop.francesinha.auth.common.TestUtils;
import shop.francesinha.auth.model.User;
import shop.francesinha.auth.service.UserService;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIT {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoSpyBean
    protected UserService userService;

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
        Mockito.verify(userService, Mockito.times(1)).registerUser(username, password);
        Mockito.verify(userService, Mockito.times(1)).findByUsername(username);

        User user = userService.findByUsername(username);
        assertNotNull(user);
        assertEquals(username, user.getUsername());
        assertNotNull(user.getEncryptedPassword());
        assertFalse(user.getEncryptedPassword().isEmpty());
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
        Mockito.verify(userService, Mockito.times(1)).registerUser(username, password);
        Mockito.verify(userService, Mockito.atLeastOnce()).findByUsername(username);
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
        Mockito.verify(userService, Mockito.times(2)).registerUser(username, password);
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
        Mockito.verify(userService, Mockito.atLeastOnce()).findByUsername(username);
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
}
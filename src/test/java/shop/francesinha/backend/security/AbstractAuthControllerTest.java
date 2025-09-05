package shop.francesinha.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class AbstractAuthControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    protected ResultActions registerUser(String username, String password) throws Exception {
        return mockMvc.perform(post("/auth/register")
                .with(csrf().asHeader()) // optional if CSRF disabled
                .param("username", username)
                .param("password", password)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED));
    }

    protected ResultActions loginUser(String username, String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .with(csrf().asHeader()) // optional if CSRF disabled
                .param("username", username)
                .param("password", password)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED));
    }

    protected ResultActions postEndpointWithToken(String endpoint, String token) throws Exception {
        return mockMvc.perform(post(endpoint)
                .header("Authorization", "Bearer " + token));
    }

    protected ResultActions getEndpointWithToken(String endpoint, String token) throws Exception {
        return mockMvc.perform(get(endpoint)
                .header("Authorization", "Bearer " + token));
    }
}
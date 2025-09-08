package shop.francesinha.backend.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shop.francesinha.backend.model.User;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class TestUtils {

    public static ResultActions registerUser(MockMvc mockMvc, String username, String password) throws Exception {
        return mockMvc.perform(post("/auth/register")
                .with(csrf().asHeader()) // optional if CSRF disabled
                .param("username", username)
                .param("password", password)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED));
    }

    public static ResultActions loginUser(MockMvc mockMvc, String username, String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                .with(csrf().asHeader()) // optional if CSRF disabled
                .param("username", username)
                .param("password", password)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED));
    }

    public static ResultActions postEndpointWithToken(MockMvc mockMvc, String endpoint, String token) throws Exception {
        return mockMvc.perform(post(endpoint)
                .header("Authorization", "Bearer " + token));
    }

    public static ResultActions getEndpointWithToken(MockMvc mockMvc, String endpoint, String token) throws Exception {
        return mockMvc.perform(get(endpoint)
                .header("Authorization", "Bearer " + token));
    }

    public static ResultActions deleteEndpointWithToken(MockMvc mockMvc, String endpoint, String token) throws Exception {
        return mockMvc.perform(delete(endpoint)
                .header("Authorization", "Bearer " + token));
    }

    public static ResultActions putEndpointWithToken(MockMvc mockMvc, String endpoint, Object object, String token) throws Exception {
        return mockMvc.perform(put(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(object))
                .header("Authorization", "Bearer " + token));
    }
}
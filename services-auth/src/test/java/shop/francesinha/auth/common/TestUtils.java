package shop.francesinha.auth.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

public class TestUtils {

    private static ResultActions resultActions;

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

    public static ResultActions postEndpoint(MockMvc mockMvc, String endpoint, Object object) throws Exception {
        return postEndpointWithToken(mockMvc, endpoint, object, "");
    }

    public static ResultActions postEndpointWithToken(MockMvc mockMvc, String endpoint, Object object, String token) throws Exception {
        return mockMvc.perform(post(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(object != null ? new ObjectMapper().writeValueAsString(object) : "")
                .header("Authorization", "Bearer " + token));
    }

    public static ResultActions getEndpoint(MockMvc mockMvc, String endpoint) throws Exception {
        return getEndpointWithToken(mockMvc, endpoint, "");
    }

    public static ResultActions getEndpointWithToken(MockMvc mockMvc, String endpoint, String token) throws Exception {
        return mockMvc.perform(get(endpoint)
                .header("Authorization", "Bearer " + token));
    }

    public static ResultActions deleteEndpoint(MockMvc mockMvc, String endpoint) throws Exception {
        return deleteEndpointWithToken(mockMvc, endpoint, "");
    }

    public static ResultActions deleteEndpointWithToken(MockMvc mockMvc, String endpoint, String token) throws Exception {
        return mockMvc.perform(delete(endpoint)
                .header("Authorization", "Bearer " + token));
    }

    public static ResultActions putEndpoint(MockMvc mockMvc, String endpoint, Object object) throws Exception {
        return putEndpointWithToken(mockMvc, endpoint, object, "");
    }

    public static ResultActions putEndpointWithToken(MockMvc mockMvc, String endpoint, Object object, String token) throws Exception {
        return mockMvc.perform(put(endpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(object))
                .header("Authorization", "Bearer " + token));
    }
}
package shop.francesinha.backend.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import shop.francesinha.backend.common.TestContainerConfig;
import shop.francesinha.backend.common.TestUtils;
import shop.francesinha.backend.model.User;
import shop.francesinha.backend.repo.IUserRepository;
import shop.francesinha.backend.security.CustomUserDetailsService;
import shop.francesinha.backend.security.JwtUtils;

import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestContainerConfig.class)
@AutoConfigureMockMvc
public class UserControllerAdminIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private JwtUtils jwtUtils;

    @Test
    public void shouldNotDeleteLastAdmin() throws Exception {
        List<User> admins = userDetailsService.getUsersByRole("ADMIN");

        for (User admin : admins) {
            String dummyToken = "token for " + admin.getUsername();

            Mockito.when(jwtUtils.isTokenValid(dummyToken)).thenReturn(true);
            Mockito.when(jwtUtils.getUsernameFromToken(dummyToken)).thenReturn(admin.getUsername());

            ResultActions resultActions = TestUtils.deleteEndpointWithToken(mockMvc, "/user/" + admin.getUsername(), dummyToken);

            if (admins.size() > 1) {
                resultActions.andExpect(status().isOk())
                        .andExpect(content().json("{\"message\":\"User deleted successfully\"}"));
            } else {
                resultActions.andExpect(status().isBadRequest())
                        .andExpect(content().json("{\"message\":\"Cannot remove last admin\"}"));
            }
        }
    }

    @Test
    public void shouldNotRemoveLastAdminRole() throws Exception {
        List<User> admins = userDetailsService.getUsersByRole("ADMIN");

        for (User admin : admins) {
            String dummyToken = "token for " + admin.getUsername();
            Mockito.when(jwtUtils.isTokenValid(dummyToken)).thenReturn(true);
            Mockito.when(jwtUtils.getUsernameFromToken(dummyToken)).thenReturn(admin.getUsername());
            admin.setRoles(Set.of("USER")); // Attempt to remove ADMIN role
            ResultActions resultActions = TestUtils.putEndpointWithToken(mockMvc, "/user", admin, dummyToken);

            if (admins.size() > 1) {
                resultActions.andExpect(status().isOk())
                        .andExpect(jsonPath("$.roles").value(Set.of("USER")));
            } else {
                resultActions.andExpect(status().isBadRequest())
                        .andExpect(content().json("{\"message\":\"Cannot demote last admin\"}"));
            }
        }
    }
}
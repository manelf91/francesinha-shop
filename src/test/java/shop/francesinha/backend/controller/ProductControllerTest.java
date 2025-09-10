package shop.francesinha.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.servlet.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import shop.francesinha.backend.model.Product;
import shop.francesinha.backend.repo.ProductRepository;
import shop.francesinha.backend.security.JwtAuthenticationFilter;
import shop.francesinha.backend.security.JwtUtils;
import shop.francesinha.backend.service.ProductService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getProducts_ReturnsList() throws Exception {
        Product p = new Product();
        p.setId(1L);
        Mockito.when(productService.getProducts()).thenReturn(List.of(p));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(p))));
    }

    @Test
    void getProductById_ReturnsProduct() throws Exception {
        Product p = new Product();
        p.setId(2L);
        Mockito.when(productService.getProduct(2L)).thenReturn(p);

        mockMvc.perform(get("/products/2"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(p)));
    }

    @Test
    void getProductById_NotFound() throws Exception {
        Mockito.when(productService.getProduct(99L)).thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(get("/products/99"))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertEquals("Product not found", Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void saveProduct_ReturnsProduct() throws Exception {
        Product p = new Product();
        p.setName("New Product");
        Mockito.when(productService.saveProduct(Mockito.any(Product.class))).thenReturn(p);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(p)));
    }

    @Test
    void saveProduct_NullName_ReturnsBadRequest() throws Exception {
        Product p = new Product();
        p.setName(null);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(Objects.requireNonNull(result.getResolvedException()).getMessage().contains("Name must not be empty")));
    }

    @Test
    void updateProduct_ReturnsProduct() throws Exception {
        Product p = new Product();
        p.setId(3L);
        p.setName("Updated Product");
        Mockito.doNothing().when(productService).updateProduct(Mockito.any(Product.class));

        mockMvc.perform(put("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteProduct_DeletesProduct() throws Exception {
        Mockito.doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteProduct_NotFound() throws Exception {
        Mockito.doThrow(new RuntimeException("Product with ID 9999 does not exist."))
                .when(productService).deleteProduct(9999L);

        mockMvc.perform(delete("/products/9999"))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertEquals("Product with ID 9999 does not exist.",
                        Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    void deleteProduct_NullId() throws Exception {
        // Simulate controller handling of null id (e.g., /products/null)
        mockMvc.perform(delete("/products/null"));
    }
}
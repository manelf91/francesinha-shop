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

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getAllProducts_ReturnsProducts() throws Exception {
        Product p = new Product();
        Mockito.when(productRepository.findAll()).thenReturn(List.of(p));

        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(p))));
    }

    @Test
    public void getAllProducts_ReturnsEmptyList() throws Exception {
        Mockito.when(productRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/products")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of())));
    }

    @Test
    public void getProductById_ReturnsProduct() throws Exception {
        Product p = new Product();
        p.setId(1L);
        Mockito.when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(p));

        mockMvc.perform(get("/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(p)));
    }

    @Test
    public void getProductById_NotFound() throws Exception {
        Mockito.when(productRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertEquals("Product with ID 1 not found.", Objects.requireNonNull(result.getResolvedException()).getMessage()));
    }

    @Test
    public void saveProduct_SavesAndReturnsProduct() throws Exception {
        Product p = new Product();
        p.setName("Test Product");
        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(p);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(p)));

        Mockito.verify(productRepository).save(Mockito.argThat(prod -> "Test Product".equals(p.getName())));
    }

    @Test
    public void saveProduct_AlreadyExists() throws Exception {
        Product p = new Product();
        p.setId(1L); // Set a valid ID
        p.setName("Test Product");
        Mockito.when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(p));

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertEquals("Product with ID 1 already exists.", Objects.requireNonNull(result.getResolvedException()).getMessage()));

        Mockito.verify(productRepository, Mockito.never()).save(Mockito.any(Product.class));
    }

    @Test
    public void saveProduct_ErrorEmptyName() throws Exception {
        Product p = new Product();
        p.setName(""); // Empty name

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(Objects.requireNonNull(result.getResolvedException()).getMessage().contains("Name must not be empty")));

        Mockito.verify(productRepository, Mockito.never()).save(Mockito.any(Product.class));
    }

    @Test
    public void updateProduct() throws Exception {
        Product p = new Product();
        p.setId(1L);
        p.setName("Updated Product");
        Mockito.when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(p));
        Mockito.when(productRepository.save(Mockito.any(Product.class))).thenReturn(p);

        mockMvc.perform(put("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(p)));

        Mockito.verify(productRepository).save(Mockito.argThat(prod -> prod.getId().equals(p.getId())));
    }

    @Test
    public void updateProduct_ErrorNotFound() throws Exception {
        Product p = new Product();
        p.setId(1L);
        p.setName("Updated Product");

        mockMvc.perform(put("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertEquals("Product with ID 1 does not exist.", Objects.requireNonNull(result.getResolvedException()).getMessage()));

        Mockito.verify(productRepository, Mockito.never()).save(Mockito.any(Product.class));
    }

    @Test
    public void updateProduct_ErrorEmptyName() throws Exception {
        Product p = new Product();
        p.setId(1L);
        p.setName(""); // Empty name
        Mockito.when(productRepository.findById(1L)).thenReturn(java.util.Optional.of(p));

        mockMvc.perform(put("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(Objects.requireNonNull(result.getResolvedException()).getMessage().contains("Name must not be empty")));

        Mockito.verify(productRepository, Mockito.never()).save(Mockito.any(Product.class));
    }

    @Test
    public void updateProduct_ErrorNullId() throws Exception {
        Product p = new Product();
        p.setId(null); // Null ID
        p.setName("Updated Product");
        mockMvc.perform(put("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isInternalServerError())
                .andExpect(result -> assertEquals("Product ID must not be null.", Objects.requireNonNull(result.getResolvedException()).getMessage()));

        Mockito.verify(productRepository, Mockito.never()).save(Mockito.any(Product.class));
    }
}

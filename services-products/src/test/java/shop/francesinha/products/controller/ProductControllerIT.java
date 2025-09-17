package shop.francesinha.products.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import shop.francesinha.products.model.Product;
import shop.francesinha.products.repo.ProductRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    JwtDecoder jwtDecoder;  // <— satisfies WebSecurityConfig

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void getProducts_ReturnsList() throws Exception {
        Product p = new Product();
        p.setName("Test Product");
        productRepository.save(p);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    void getProductById_ReturnsProduct() throws Exception {
        Product p = new Product();
        p.setName("Test Product");
        Product saved = productRepository.save(p);

        mockMvc.perform(get("/products/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void saveProduct_CreatesProduct() throws Exception {
        Product p = new Product();
        p.setId(null);
        p.setName("New Product");

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Product"));

        List<Product> products = productRepository.findAll();
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("New Product");
    }

    @Test
    void updateProduct_UpdatesProduct() throws Exception {
        Product p = new Product();
        p.setName("Old Name");
        Product saved = productRepository.save(p);

        saved.setName("Updated Name");

        mockMvc.perform(put("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isOk());

        Product updated = productRepository.findById(saved.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void deleteProduct_DeletesProduct() throws Exception {
        Product p = new Product();
        p.setName("To Delete");
        Product saved = productRepository.save(p);

        mockMvc.perform(delete("/products/" + saved.getId()))
                .andExpect(status().isOk());

        assertThat(productRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void deleteProduct_NotFound() throws Exception {
        mockMvc.perform(delete("/products/9999"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Product with ID 9999 does not exist."));
    }

    @Test
    void deleteProduct_NullId() throws Exception {
        mockMvc.perform(delete("/products/null"))
                .andExpect(status().isBadRequest());
    }
}

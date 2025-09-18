package shop.francesinha.e2e;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class E2ETests {

    private static String authToken;
    private static ProductDTO savedProduct;
    private static final String authEndpoint = System.getenv().getOrDefault("AUTH_ENDPOINT", "http://localhost:8080");
    private static final String productEndpoint = System.getenv().getOrDefault("PRODUCT_ENDPOINT", "http://localhost:8081");
    private static final String reviewEndpoint = System.getenv().getOrDefault("REVIEW_ENDPOINT", "http://localhost:8082");
    private static WebClient productClient;
    private static WebClient reviewClient;

    @BeforeAll
    static void setup() {
        login();
        assertNotNull(authToken, "JWT token must not be null");

        // Create a product
        ProductDTO product = new ProductDTO(null, "Test Product", 9.99);
        savedProduct = getProductWebClient().post()
                .uri("/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .bodyValue(product)
                .retrieve()
                .bodyToMono(ProductDTO.class)
                .block();
        assertNotNull(savedProduct);
        assertNotNull(savedProduct.id());
    }

    @Test
    void testProductExistsAndReviewCanBeCreated() {
        // Post a review for that product
        ReviewDTO review = new ReviewDTO(savedProduct.id(), "cust01", 5, "Excellent product!");
        ReviewDTO savedReview = getReviewClient().post()
                .uri("/reviews")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .bodyValue(review)
                .retrieve()
                .bodyToMono(ReviewDTO.class)
                .block();

        assertNotNull(savedReview);
        assertEquals(savedProduct.id(), savedReview.productId());
        assertEquals("cust01", savedReview.customerId());
    }

    private static WebClient getReviewClient() {
        if (reviewClient == null) {
            reviewClient = getWebClient(reviewEndpoint);
        }
        return reviewClient;
    }

    private static WebClient getProductWebClient() {
        if (productClient == null) {
            productClient = getWebClient(productEndpoint);
        }
        return productClient;
    }

    private static void login() {
        // WebClient pointing to local services
        WebClient webClient; webClient = WebClient.builder()
                .baseUrl(authEndpoint) // auth service base
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        // Login and get JWT token from auth service
        Map<String, String> response = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/auth/login")
                        .queryParam("username", "admin")
                        .queryParam("password", "1234")
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {})
                .block();

        assertNotNull(response);
        authToken = response.get("token");
    }

    private static WebClient getWebClient(String url) {
        return WebClient.builder()
                .baseUrl(url)
                .build();
    }
}
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
    private static final String authEndpoint = System.getenv().getOrDefault("AUTH_ENDPOINT", "http://localhost:8080");
    private static final String productEndpoint = System.getenv().getOrDefault("PRODUCT_ENDPOINT", "http://localhost:8081");
    private static final String reviewEndpoint = System.getenv().getOrDefault("REVIEW_ENDPOINT", "http://localhost:8082");
    private static final String gatewayEndpoint = System.getenv().getOrDefault("GATEWAY_ENDPOINT", "http://localhost:8083");
    private static WebClient productClient;
    private static WebClient reviewClient;

    @BeforeAll
    static void setup() {
        login();
        assertNotNull(authToken, "JWT token must not be null");
    }

    @Test
    void testProductExistsAndReviewCanBeCreated() {
        // Create a product
        ProductDTO savedProduct = createProduct();
        assertNotNull(savedProduct);
        assertNotNull(savedProduct.id());

        // Post a review for that product
        ReviewDTO savedReview = createReview(savedProduct);

        assertNotNull(savedReview);
        assertNotNull(savedReview.id());
        assertEquals(savedProduct.id(), savedReview.productId());
    }

    @Test
    void testDeleteProductAndReviewGetsDeleted() throws InterruptedException {
        // Create a product
        ProductDTO savedProduct = createProduct();
        assertNotNull(savedProduct);
        assertNotNull(savedProduct.id());

        // Post a review for that product
        ReviewDTO savedReview = createReview(savedProduct);
        assertNotNull(savedReview);
        assertNotNull(savedReview.id());

        // Delete the product
        getProductWebClient().delete()
                .uri("/products/{id}", savedProduct.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .retrieve()
                .toBodilessEntity()
                .block();

        Integer productStatus = getProductWebClient().get()
                .uri("/products/{id}", savedProduct.id())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .exchangeToMono(response -> response.toBodilessEntity().map(r -> response.statusCode().value()))
                .block();

        assertNotNull(productStatus, "Product status should not be null");
        int status = productStatus; // Safe unboxing after null-check
        assertTrue(status == 404 || status == 410,
                "Expected 404/410 for deleted product but got " + productStatus);

        int maxRetries = 10;
        int retry = 0;
        Integer reviewStatus = null;

        while (retry < maxRetries) {
            reviewStatus = getReviewWebClient().get()
                    .uri("/reviews/{id}", savedReview.id())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                    .exchangeToMono(response -> response.toBodilessEntity().map(r -> response.statusCode().value()))
                    .block();

            if (reviewStatus == 404 || reviewStatus == 410) {
                break; // review deleted
            }

            Thread.sleep(500); // wait 0.5s before retry
            retry++;
        }

        assertNotNull(reviewStatus, "Review status should not be null");
        status = reviewStatus; // Safe unboxing after null-check
        assertTrue(status == 404 || status == 410,
                "Expected 404/410 for deleted review but got " + reviewStatus);
    }

    @Test
    void testGatewayAccess() {
        WebClient gatewayClient = getWebClient(gatewayEndpoint);

        Integer status = gatewayClient.get()
                .uri("/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .exchangeToMono(response -> response.toBodilessEntity().map(r -> response.statusCode().value()))
                .block();

        assertNotNull(status, "Gateway products status should not be null");
        int s = status; // Safe unboxing after null-check
        assertEquals(200, s, "Expected 200 from gateway but got " + status);
    }

    private static ReviewDTO createReview(ProductDTO product) {
        ReviewDTO review = new ReviewDTO(product.id(), "cust01", 5, "Excellent product!");
        return getReviewWebClient().post()
                .uri("/reviews")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .bodyValue(review)
                .retrieve()
                .bodyToMono(ReviewDTO.class)
                .block();
    }

    private static ProductDTO createProduct() {
        ProductDTO product = new ProductDTO(null, "Test Product", 9.99);
        return getProductWebClient().post()
                .uri("/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken)
                .bodyValue(product)
                .retrieve()
                .bodyToMono(ProductDTO.class)
                .block();
    }

    private static WebClient getReviewWebClient() {
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
        WebClient webClient;
        webClient = WebClient.builder()
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
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                })
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
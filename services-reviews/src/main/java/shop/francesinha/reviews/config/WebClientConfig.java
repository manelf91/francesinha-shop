package shop.francesinha.reviews.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient productServiceWebClient() {
        return WebClient.builder()
                .baseUrl(this.getProductServiceUrl()) // Product Service URL
                .build();
    }

    private String getProductServiceUrl() {
        return System.getenv().getOrDefault("PRODUCT_ENDPOINT", "http://localhost:8081");
    }
}

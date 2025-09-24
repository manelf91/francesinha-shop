package shop.francesinha.reviews.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import shop.francesinha.reviews.dto.ProductDTO;
import shop.francesinha.reviews.exception.ReviewNotFoundException;
import shop.francesinha.reviews.model.Review;
import shop.francesinha.reviews.repo.ReviewRepository;

import java.util.List;
import java.util.Map;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private WebClient webClient;

    private final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public Review getReviewById(String id) {
        return reviewRepository.findById(id).orElseThrow(() -> new ReviewNotFoundException(id));
    }

    public Review saveReview(Review review) {
        boolean productExists = this.productExists(review.getProductId());
        if (!productExists) throw new RuntimeException("Product does not exist");

        return reviewRepository.save(review);
    }

    public void updateReview(Review review) {
        if (review.getId() == null) {
            throw new RuntimeException("Review ID must not be null.");
        }
        if (reviewRepository.findById(review.getId()).isEmpty()) {
            throw new RuntimeException("Review with ID " + review.getId() + " does not exist.");
        }
        boolean productExists = this.productExists(review.getProductId());
        if (!productExists) throw new RuntimeException("Product does not exist");

        reviewRepository.save(review);
    }

    public void deleteReview(String id) {
        reviewRepository.deleteById(id);
    }

    public boolean productExists(String productId) {
        try {
            // Extract JWT from current authenticated user
            JwtAuthenticationToken auth = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            String token = auth.getToken().getTokenValue();

            ((WebClient.RequestHeadersSpec<?>) webClient.get()
                    .uri("/products/{id}", productId)
                    .headers(headers -> headers.setBearerAuth(token)))
                    .retrieve()
                    .bodyToMono(ProductDTO.class)
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @KafkaListener(topics = "product-deleted")
    public void onProductDeleted(Map<String, Object> event) {
        String productId = event.get("productId").toString();
        // idempotent deletion
        int deletedReviews =  reviewRepository.deleteByProductId(productId);
        logger.info("Deleted {} reviews for product {}", deletedReviews, productId);
    }
}
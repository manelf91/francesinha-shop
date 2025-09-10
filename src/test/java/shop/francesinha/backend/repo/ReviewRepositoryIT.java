package shop.francesinha.backend.repo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import shop.francesinha.backend.model.Review;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@Testcontainers
public class ReviewRepositoryIT {

    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private ReviewRepository reviewRepository;

    static {
        mongoDBContainer.start();
    }

    @Test
    void shouldPersistReview() {
        Review review = new Review();
        review.setCustomerId("customer123");
        review.setProductId("product456");
        review.setRating(5);
        review.setComment("Amazing product!");

        Review saved = reviewRepository.save(review);
        assertThat(saved.getId()).isNotNull();

        Review savedReview = reviewRepository.findById(saved.getId()).orElse(null);
        assertNotNull(savedReview);
        assertThat(savedReview.getComment()).isEqualTo("Amazing product!");
    }
}


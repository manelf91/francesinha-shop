package shop.francesinha.reviews.kafka;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import shop.francesinha.reviews.repo.ReviewRepository;
import shop.francesinha.reviews.service.ReviewService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "product-deleted" })
@AutoConfigureMockMvc(addFilters = false)
public class KafkaServiceIT {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    JwtDecoder jwtDecoder;  // <— satisfies WebSecurityConfig

    @Autowired
    private ReviewService reviewService;

    @MockitoBean
    private ReviewRepository reviewRepository;

    @Test
    void testKafkaServiceReceivesMessage() throws Exception {
        String productId = "1";
        Map<String, Object> event = Map.of(
                "productId", productId,
                "deletedAt", System.currentTimeMillis()
        );
        kafkaTemplate.send("product-deleted", productId, event);

        // Wait for the message to be processed
        Awaitility.await().untilAsserted(() ->
                Mockito.verify(reviewRepository, Mockito.times(1)).deleteByProductId(productId)
        );
    }
}
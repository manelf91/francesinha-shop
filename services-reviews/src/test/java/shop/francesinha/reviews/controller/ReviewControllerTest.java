package shop.francesinha.reviews.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import shop.francesinha.reviews.common.TestUtils;
import shop.francesinha.reviews.model.Review;
import shop.francesinha.reviews.repo.ReviewRepository;

import java.util.Optional;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewRepository reviewRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllReviews() throws Exception {
        Review review = new Review();
        String id = String.valueOf(1L);
        review.setId(id);
        Mockito.when(reviewRepository.findAll()).thenReturn(List.of(review));

        TestUtils.getEndpoint(mockMvc, "/reviews")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    void testGetReviewById() throws Exception {
        Review review = new Review();
        String id = String.valueOf(2L);
        review.setId(id);
        Mockito.when(reviewRepository.findById(id)).thenReturn(Optional.of(review));

        TestUtils.getEndpoint(mockMvc, "/reviews/2")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void testSaveReview() throws Exception {
        Review review = new Review();
        review.setComment("lovely product!");
        review.setCustomerId("cust123");
        review.setRating(4);
        review.setProductId("prod456");
        Mockito.when(reviewRepository.save(Mockito.any())).thenReturn(review);

        TestUtils.postEndpoint(mockMvc, "/reviews", review).andExpect(status().isOk());
    }

    @Test
    void testUpdateReview() throws Exception {
        Review review = new Review();
        String id = String.valueOf(3L);
        review.setId(id);
        review.setComment("lovely product!");
        review.setCustomerId("cust123");
        review.setRating(4);
        review.setProductId("prod456");
        Mockito.when(reviewRepository.findById(id)).thenReturn(Optional.of(review));
        Mockito.when(reviewRepository.save(Mockito.any())).thenReturn(review);

        TestUtils.putEndpoint(mockMvc, "/reviews", review).andExpect(status().isOk());
    }
}

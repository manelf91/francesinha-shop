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
import shop.francesinha.reviews.exception.ReviewNotFoundException;
import shop.francesinha.reviews.model.Review;
import shop.francesinha.reviews.service.ReviewService;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllReviews() throws Exception {
        Review review = new Review();
        String id = String.valueOf(1L);
        review.setId(id);
        Mockito.when(reviewService.getAllReviews()).thenReturn(List.of(review));

        TestUtils.getEndpoint(mockMvc, "/reviews")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));
    }

    @Test
    void testGetReviewById() throws Exception {
        Review review = new Review();
        String id = "2";
        review.setId(id);
        Mockito.when(reviewService.getReviewById(id)).thenReturn(review);

        TestUtils.getEndpoint(mockMvc, "/reviews/" + id)
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
        Mockito.when(reviewService.saveReview(Mockito.any())).thenReturn(review);

        TestUtils.postEndpoint(mockMvc, "/reviews", review).andExpect(status().isOk());
    }

    @Test
    void testUpdateReview() throws Exception {
        Review review = new Review();
        String id = "3L";
        review.setId(id);
        review.setComment("lovely product!");
        review.setCustomerId("cust123");
        review.setRating(4);
        review.setProductId("prod456");
        Mockito.when(reviewService.getReviewById(id)).thenReturn(review);
        Mockito.doNothing().when(reviewService).updateReview(Mockito.any());

        TestUtils.putEndpoint(mockMvc, "/reviews", review).andExpect(status().isOk());
    }

    @Test
    void testDeleteReview() throws Exception {
        Mockito.doNothing().when(reviewService).deleteReview("4");
        TestUtils.deleteEndpoint(mockMvc, "/reviews/4").andExpect(status().isOk());
    }

    @Test
    void testSaveReview_NullProduct_ReturnsBadRequest() throws Exception {
        Review review = new Review();
        review.setComment("Nice");
        review.setCustomerId("cust123");
        review.setRating(5);
        // ProductId is null

        TestUtils.postEndpoint(mockMvc, "/reviews", review)
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(Objects.requireNonNull(result.getResolvedException()).getMessage().contains("Product cannot be null")));
    }

    @Test
    void testDeleteReview_NotFound() throws Exception {
        String id = "99";
        Mockito.doThrow(new ReviewNotFoundException(id)).when(reviewService).deleteReview(id);
        TestUtils.deleteEndpoint(mockMvc, "/reviews/" + id)
                .andExpect(status().isNotFound())
                .andExpect(result -> assertTrue(Objects.requireNonNull(result.getResolvedException()).getMessage().contains("Review with ID " + id + " not found")));
    }
}
package shop.francesinha.reviews.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import shop.francesinha.reviews.model.Review;
import shop.francesinha.reviews.repo.ReviewRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void testGetAllReviews() {
        Review review = new Review();
        review.setId("1");
        review.setComment("Good");
        review.setRating(5);
        when(reviewRepository.findAll()).thenReturn(List.of(review));

        List<Review> result = reviewService.getAllReviews();

        assertEquals(1, result.size());
        assertEquals("Good", result.get(0).getComment());
    }

    @Test
    void testGetReviewByIdFound() {
        Review review = new Review();
        review.setId("1");
        review.setComment("Test");
        when(reviewRepository.findById("1")).thenReturn(Optional.of(review));

        Review result = reviewService.getReviewById(1L);
        assertEquals("Test", result.getComment());
    }

    @Test
    void testGetReviewByIdNotFound() {
        when(reviewRepository.findById("2")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> reviewService.getReviewById(2L));
    }

    @Test
    void testSaveReviewProductExists() {
        Review review = new Review();
        review.setProductId("prod1");
        // Mock productExists to return true
        ReviewService spyService = Mockito.spy(reviewService);
        doReturn(true).when(spyService).productExists("prod1");
        when(reviewRepository.save(review)).thenReturn(review);

        Review result = spyService.saveReview(review);
        assertEquals(review, result);
    }

    @Test
    void testSaveReviewProductNotExists() {
        Review review = new Review();
        review.setProductId("prod2");
        ReviewService spyService = Mockito.spy(reviewService);
        doReturn(false).when(spyService).productExists("prod2");

        assertThrows(RuntimeException.class, () -> spyService.saveReview(review));
    }

    @Test
    void testUpdateReviewSuccess() {
        Review review = new Review();
        review.setId("1");
        review.setProductId("prod1");
        ReviewService spyService = Mockito.spy(reviewService);
        doReturn(true).when(spyService).productExists("prod1");
        when(reviewRepository.findById("1")).thenReturn(Optional.of(review));
        when(reviewRepository.save(review)).thenReturn(review);

        assertDoesNotThrow(() -> spyService.updateReview(review));
    }

    @Test
    void testUpdateReviewNullId() {
        Review review = new Review();
        review.setId(null);

        assertThrows(RuntimeException.class, () -> reviewService.updateReview(review));
    }

    @Test
    void testUpdateReviewNotFound() {
        Review review = new Review();
        review.setId("2");
        when(reviewRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reviewService.updateReview(review));
    }

    @Test
    void testUpdateReviewProductNotExists() {
        Review review = new Review();
        review.setId("1");
        review.setProductId("prod2");
        ReviewService spyService = Mockito.spy(reviewService);
        doReturn(false).when(spyService).productExists("prod2");
        when(reviewRepository.findById("1")).thenReturn(Optional.of(review));

        assertThrows(RuntimeException.class, () -> spyService.updateReview(review));
    }

    @Test
    void testDeleteReview() {
        doNothing().when(reviewRepository).deleteById("1");

        assertDoesNotThrow(() -> reviewService.deleteReview(1L));
        verify(reviewRepository, times(1)).deleteById("1");
    }
}
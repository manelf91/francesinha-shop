package shop.francesinha.reviews.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import shop.francesinha.reviews.model.Review;
import shop.francesinha.reviews.service.ReviewService;

import java.util.List;

@RequestMapping("/reviews")
@RestController
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id);
    }

    @PostMapping
    public Review saveReview(@Valid @RequestBody Review review) {
        return reviewService.saveReview(review);
    }

    @PutMapping
    public void updateReview(@Valid @RequestBody Review review) {
        reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
    }
}

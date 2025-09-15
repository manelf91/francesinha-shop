package shop.francesinha.backend.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import shop.francesinha.backend.model.Review;
import shop.francesinha.backend.repo.ReviewRepository;

import java.util.List;

@RequestMapping("/reviews")
@RestController
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable Long id) {
        return reviewRepository.findById(String.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Review with ID " + id + " not found."));
    }

    @PostMapping
    public Review saveReview(@Valid @RequestBody Review review) {
        if (review.getId() != null && reviewRepository.findById(review.getId()).isPresent()) {
            throw new RuntimeException("Review with ID " + review.getId() + " already exists.");
        }
        return reviewRepository.save(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        if (review.getId() == null) {
            throw new RuntimeException("Review ID must not be null.");
        }
        if (reviewRepository.findById(review.getId()).isEmpty()) {
            throw new RuntimeException("Review with ID " + review.getId() + " does not exist.");
        }
        return reviewRepository.save(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable Long id) {
        if (reviewRepository.findById(String.valueOf(id)).isEmpty()) {
            throw new RuntimeException("Review with ID " + id + " does not exist.");
        }
        reviewRepository.deleteById(String.valueOf(id));
    }
}

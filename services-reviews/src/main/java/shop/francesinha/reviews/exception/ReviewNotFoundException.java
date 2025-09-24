package shop.francesinha.reviews.exception;

public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String reviewId) {
        super("Review with ID " + reviewId + " not found.");
    }
}

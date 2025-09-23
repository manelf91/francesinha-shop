package shop.francesinha.reviews.dto;

// common module or duplicated small class
public class ProductDeletedEvent {
    private Long productId;
    private long deletedAt; // epoch millis, optional

    public Long getProductId() {
        return productId;
    }
    // constructors, getters, setters
}
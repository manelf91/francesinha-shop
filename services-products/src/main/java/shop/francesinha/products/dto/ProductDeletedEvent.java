package shop.francesinha.products.dto;

public class ProductDeletedEvent {
    private Long productId;
    private long deletedAt; // epoch millis, optional

    public ProductDeletedEvent(Long productId, long deletedAt) {
        this.productId = productId;
        this.deletedAt = deletedAt;
    }
}
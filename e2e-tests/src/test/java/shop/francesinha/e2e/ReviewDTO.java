package shop.francesinha.e2e;

public record ReviewDTO(String id, String productId, String customerId, int rating, String comment) {
    public ReviewDTO(String productId, String cust01, int i, String s) {
        this(null, productId, cust01, i, s);
    }
}

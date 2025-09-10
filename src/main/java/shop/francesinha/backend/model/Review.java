package shop.francesinha.backend.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "reviews")
public class Review {

    @Id
    private String id; // MongoDB uses String for the ID field

    @NotNull(message = "Customer cannot be null")
    private String customerId; // The customer who created the review

    @NotNull(message = "Product cannot be null")
    private String productId; // The product being reviewed

    @NotNull(message = "Rating cannot be null")
    private Integer rating; // Rating out of 5

    @NotBlank(message = "Comment cannot be blank")
    private String comment; // Optional comment on the product
}

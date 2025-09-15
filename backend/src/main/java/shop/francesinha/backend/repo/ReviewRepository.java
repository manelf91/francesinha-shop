package shop.francesinha.backend.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import shop.francesinha.backend.model.Review;

public interface ReviewRepository extends MongoRepository<Review, String> {

}

package shop.francesinha.reviews.repo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import shop.francesinha.reviews.model.Review;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

}

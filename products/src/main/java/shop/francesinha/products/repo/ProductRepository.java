package shop.francesinha.products.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shop.francesinha.products.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}

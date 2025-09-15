package shop.francesinha.backend.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shop.francesinha.backend.model.Inventory;
import shop.francesinha.backend.model.Product;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}

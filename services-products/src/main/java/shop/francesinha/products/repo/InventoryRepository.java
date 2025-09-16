package shop.francesinha.products.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shop.francesinha.products.model.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}

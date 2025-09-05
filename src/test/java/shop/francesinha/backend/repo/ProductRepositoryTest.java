package shop.francesinha.backend.repo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import shop.francesinha.backend.model.Inventory;
import shop.francesinha.backend.model.Product;

@SpringBootTest
@Testcontainers
public class ProductRepositoryTest extends AbstractJPARepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void shouldPersistProduct() {
        var product = new Product();
        product.setName("Test Product");
        product.setPrice(9.99);

        var savedProduct = productRepository.save(product);

        assert savedProduct.getId() != null;
        assert productRepository.findById(savedProduct.getId()).isPresent();
    }

    @Test
    void shouldPersistProductWithInventoryCascaded() {
        var product = new Product();
        product.setName("Test Product with Inventory");
        product.setPrice(19.99);

        var inventory = new Inventory();
        inventory.setProduct(product);
        product.setInventories(java.util.List.of(inventory));

        var savedProduct = productRepository.save(product);

        assert savedProduct.getId() != null;
        assert productRepository.findById(savedProduct.getId()).isPresent();
        assert savedProduct.getInventories() != null;
        assert savedProduct.getInventories().size() == 1;

        Inventory savedInventory = savedProduct.getInventories().get(0);
        assert savedInventory.getId() != null;
        assert savedInventory.getProduct().getId().equals(savedProduct.getId());
        assert inventoryRepository.findById(savedInventory.getId()).isPresent();
    }

    @Test
    public void shouldDeleteProductAndCascadeInventory() {
        Product product = new Product();
        product.setName("Product to Delete");
        product.setPrice(29.99);

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        product.setInventories(java.util.List.of(inventory));

        Product savedProduct = productRepository.save(product);
        Long productId = savedProduct.getId();
        Long inventoryId = savedProduct.getInventories().get(0).getId();

        // Ensure both product and inventory are saved
        assert productRepository.findById(productId).isPresent();
        assert inventoryRepository.findById(inventoryId).isPresent();

        // Delete the product
        productRepository.deleteById(productId);
        // Ensure both product and inventory are deleted
        assert productRepository.findById(productId).isEmpty();
        assert inventoryRepository.findById(inventoryId).isEmpty();
    }
}

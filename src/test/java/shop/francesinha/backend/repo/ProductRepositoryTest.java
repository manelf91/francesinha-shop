package shop.francesinha.backend.repo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import shop.francesinha.backend.model.Inventory;
import shop.francesinha.backend.model.Product;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductRepositoryTest extends AbstractJPARepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", AbstractJPARepositoryTest::getJdbcUrl);
        registry.add("spring.datasource.username", AbstractJPARepositoryTest::getUsername);
        registry.add("spring.datasource.password", AbstractJPARepositoryTest::getPassword);
    }

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
}

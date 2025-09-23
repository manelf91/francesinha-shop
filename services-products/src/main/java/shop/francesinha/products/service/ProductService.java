package shop.francesinha.products.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import shop.francesinha.products.dto.ProductDeletedEvent;
import shop.francesinha.products.exception.ProductNotFoundException;
import shop.francesinha.products.model.Product;
import shop.francesinha.products.repo.ProductRepository;


import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ProductService {

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private KafkaTemplate<String, ProductDeletedEvent> kafkaTemplate;

    @Cacheable(cacheNames = "products")
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public void updateProduct(Product product) {
        if (product.getId() == null) {
            throw new RuntimeException("Product ID must not be null.");
        }
        if (productRepository.findById(product.getId()).isEmpty()) {
            throw new ProductNotFoundException(product.getId());
        }
        productRepository.save(product);
    }

    @Transactional
    @CacheEvict(cacheNames = "products", allEntries = true)
    public void deleteProduct(Long productId) {
        if (productId == null) {
            throw new RuntimeException("Product ID must not be null.");
        }
        if (productRepository.findById(productId).isEmpty()) {
            throw new ProductNotFoundException(productId);
        }
        productRepository.deleteById(productId);

//
//        // 2) Publish event (asynchronously)
//        ProductDeletedEvent ev = new ProductDeletedEvent(productId, System.currentTimeMillis());
//        CompletableFuture<SendResult<String, ProductDeletedEvent>> future =
//                kafkaTemplate.send("product-deleted", productId.toString(), ev);
//
//        // optional: add callback
//        future.whenComplete((result, ex) -> {
//            if (ex == null) {
//                logger.info("Product-deleted event sent for {}", productId);
//            } else {
//                logger.error("Failed to publish product-deleted for {}: {}", productId, ex.getMessage());
//                // Possible strategies:
//                // - Trigger a retry mechanism
//                // - Persist the event in an "outbox" table for later reprocessing
//            }
//        });
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}

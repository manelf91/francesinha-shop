package shop.francesinha.products.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import shop.francesinha.products.exception.ProductNotFoundException;
import shop.francesinha.products.kafka.KafkaService;
import shop.francesinha.products.model.Product;
import shop.francesinha.products.repo.ProductRepository;


import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private KafkaService kafkaService;

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
        kafkaService.deleteRelatedReviews(productId);
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}

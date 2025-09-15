package shop.francesinha.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import shop.francesinha.backend.model.Product;
import shop.francesinha.backend.repo.ProductRepository;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Cacheable(cacheNames = "products")
    public List<Product> getProducts() {
        return productRepository.findAll();
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public void updateProduct(Product product) {
        if (product.getId() == null) {
            throw new RuntimeException("Product ID must not be null.");
        }
        if (productRepository.findById(product.getId()).isEmpty()) {
            throw new RuntimeException("Product with ID " + product.getId() + " does not exist.");
        }
        productRepository.save(product);
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public void deleteProduct(Long id) {
        if (id == null) {
            throw new RuntimeException("Product ID must not be null.");
        }
        if (productRepository.findById(id).isEmpty()) {
            throw new RuntimeException("Product with ID " + id + " does not exist.");
        }
        productRepository.deleteById(id);
    }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}

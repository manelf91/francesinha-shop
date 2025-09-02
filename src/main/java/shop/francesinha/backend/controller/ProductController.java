package shop.francesinha.backend.controller;

import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import shop.francesinha.backend.model.Product;
import shop.francesinha.backend.repo.ProductRepository;

import java.util.List;

@RequestMapping("/products")
@RestController
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product with ID " + id + " not found."));
    }

    @PostMapping
    public Product saveProduct(@Valid @RequestBody Product product) {
        if (productRepository.findById(product.getId()).isPresent()) {
            throw new RuntimeException("Product with ID " + product.getId() + " already exists.");
        }
        return productRepository.save(product);
    }

    @PutMapping
    public Product updateProduct(@Valid @RequestBody Product product) {
        if (product.getId() == null) {
            throw new RuntimeException("Product ID must not be null.");
        }
        if (productRepository.findById(product.getId()).isEmpty()) {
            throw new RuntimeException("Product with ID " + product.getId() + " does not exist.");
        }
        return productRepository.save(product);
    }
}

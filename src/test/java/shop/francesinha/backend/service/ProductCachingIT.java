package shop.francesinha.backend.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import shop.francesinha.backend.model.Product;
import shop.francesinha.backend.repo.ProductRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnableCaching
public class ProductCachingIT {

    @Autowired
    private ProductService productService;

    @MockitoSpyBean
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void testProductsCache() {
        Cache cache = cacheManager.getCache("products");
        assertThat(cache.get("")).isNull(); // no entry yet

        // Save a product
        Product p = new Product();
        p.setName("Cached Product");
        p.setPrice(10.0);
        productService.saveProduct(p);

        // First call - hits DB and caches the list
        List<Product> firstFetch = productService.getProducts();
        assertThat(firstFetch).hasSize(1);

        // Cache should now contain the list
        Cache.ValueWrapper wrapper = cache.get(SimpleKey.EMPTY); // fetch the cached entry
        assertThat(wrapper).isNotNull(); // first make sure it exists
        List<Product> cachedList = (List<Product>) wrapper.get(); // now safe
        assertThat(cachedList).hasSize(1);
        Mockito.clearInvocations(productRepository);

        // Second call - should hit cache (not DB)
        List<Product> secondFetch = productService.getProducts();
        assertThat(secondFetch).isSameAs(cachedList);
        Mockito.verifyNoInteractions(productRepository);

        // Update product - cache should be evicted
        p.setPrice(12.0);
        productService.updateProduct(p);
        assertThat(cache.get(SimpleKey.EMPTY)).isNull();

        // Next call - fetches fresh data
        List<Product> refreshedList = productService.getProducts();
        assertThat(refreshedList.get(0).getPrice()).isEqualTo(12.0);
    }
}

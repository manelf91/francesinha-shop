package shop.francesinha.products;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ProductServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApp.class, args);
    }
}

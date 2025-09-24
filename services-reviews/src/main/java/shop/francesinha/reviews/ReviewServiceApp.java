package shop.francesinha.reviews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class ReviewServiceApp {

    public static void main(String[] args) {
        SpringApplication.run(ReviewServiceApp.class, args);
    }
}

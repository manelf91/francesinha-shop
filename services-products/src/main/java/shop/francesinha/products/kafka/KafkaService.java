package shop.francesinha.products.kafka;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class KafkaService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    private static KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    public KafkaService (KafkaTemplate<String, Object> kafkaTemplate) {
        KafkaService.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    private void createTopicIfNotExists() {
        String topicName = "product-deleted";
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        try (AdminClient admin = AdminClient.create(props)) {
            // Check if topic already exists
            Set<String> existingTopics = admin.listTopics().names().get();
            if (!existingTopics.contains(topicName)) {
                int nPartitions = Integer.parseInt(System.getenv().getOrDefault("KAFKA_PRODUCT_DELETE_NPARTITION", "3"));
                short replicationFactor = Short.parseShort(System.getenv().getOrDefault("KAFKA_PRODUCT_DELETE_REPLICATIONFACTOR", "1"));
                NewTopic newTopic = new NewTopic(topicName, nPartitions, replicationFactor);
                admin.createTopics(Collections.singletonList(newTopic)).all().get();
                System.out.println("✅ Created Kafka topic: " + topicName);
            } else {
                System.out.println("ℹ️ Kafka topic already exists: " + topicName);
            }
        } catch (InterruptedException | ExecutionException e) {
            // Handle errors gracefully (e.g., cluster unavailable on startup)
            System.err.println("⚠️ Failed to verify/create Kafka topic '" + topicName + "': " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteRelatedReviews(Long productId) {
        //Publish event (asynchronously)
        Map<String, Object> event = Map.of(
                "productId", productId,
                "deletedAt", System.currentTimeMillis()
        );

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send("product-deleted", productId.toString(), event);

        // optional: add callback
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Product-deleted event sent for {}", productId);
            } else {
                logger.error("Failed to publish product-deleted for {}: {}", productId, ex.getMessage());
                // Possible strategies:
                // - Trigger a retry mechanism
                // - Persist the event in an "outbox" table for later reprocessing
            }
        });
    }
}

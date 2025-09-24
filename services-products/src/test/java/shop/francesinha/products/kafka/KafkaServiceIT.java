package shop.francesinha.products.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "product-deleted" })
class KafkaServiceIT {

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory;

    @MockitoBean
    JwtDecoder jwtDecoder;  // <— satisfies WebSecurityConfig

    @Test
    void testCreateTopicIfNotExists() throws Exception {
        String topicName = "product-deleted";

        // Verify topic exists
        try (AdminClient admin = AdminClient.create(Map.of("bootstrap.servers", embeddedKafka.getBrokersAsString()))) {
            ListTopicsResult topics = admin.listTopics();
            assertThat(topics.names().get()).contains(topicName);
        }
    }


    @Test
    void testProducerSendsMessageUsingProperties() throws InterruptedException {
        BlockingQueue<ConsumerRecord<String, Object>> records = new LinkedBlockingQueue<>();

        ContainerProperties containerProps = new ContainerProperties("product-deleted");

        // Use the consumer factory from the auto-configured listener factory
        KafkaMessageListenerContainer<String, Object> container =
                new KafkaMessageListenerContainer<>(
                        kafkaListenerContainerFactory.getConsumerFactory(),
                        containerProps
                );

        container.setupMessageListener((MessageListener<String, Object>) record -> {
            records.offer(record);
        });
        container.start();

        // Wait for assignment
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());

        // Send message
        Long productId = 42L;
        kafkaService.deleteRelatedReviews(productId);

        // Verify message received
        ConsumerRecord<String, Object> received = records.poll(5, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.key()).isEqualTo(productId.toString());

        Number n = (Number) ((Map<?, ?>) received.value()).get("productId");
        assertThat(n.longValue()).isEqualTo(productId);

        container.stop();
    }
}

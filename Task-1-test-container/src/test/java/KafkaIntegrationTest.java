import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
 class KafkaIntegrationTest {

    private static final String GROUP_ID = "test-group";
    private static final String TOPIC = "test-topic";

    @Container
    private ConfluentKafkaContainer kafkaContainer = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0")
            .withExposedPorts(9092);

    private Properties producerProps;
    private Properties consumerProps;

    @BeforeEach
     void setup() {
        producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        consumerProps = new Properties();
        consumerProps.put("bootstrap.servers", kafkaContainer.getBootstrapServers());
        consumerProps.put("group.id", GROUP_ID);
        consumerProps.put("key.deserializer", StringDeserializer.class.getName());
        consumerProps.put("value.deserializer", StringDeserializer.class.getName());
        consumerProps.put("auto.offset.reset", "earliest");
    }

    @Test
    void testTwoProducersSingleConsumer() {

        try (KafkaProducer<String, String> producer1 = new KafkaProducer<>(producerProps);
             KafkaProducer<String, String> producer2 = new KafkaProducer<>(producerProps);
             KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {

            // When
            consumer.subscribe(Collections.singletonList(TOPIC));
            producer1.send(new ProducerRecord<>(TOPIC, "key1", "value1"));
            producer2.send(new ProducerRecord<>(TOPIC, "key2", "value2"));

            // Act
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

            // Assert
            assertEquals(2, records.count());

        }
    }

    @Test
    void testTwoProducersSingleConsumerNoMessages() {

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {

            // When
            consumer.subscribe(Collections.singletonList(TOPIC));

            // Act
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

            // Assert
            assertEquals(0, records.count());

        }
    }

    @Test
    void testThreeProducersSingleConsumer() {

        try (KafkaProducer<String, String> producer1 = new KafkaProducer<>(producerProps);
             KafkaProducer<String, String> producer2 = new KafkaProducer<>(producerProps);
             KafkaProducer<String, String> producer3 = new KafkaProducer<>(producerProps);
             KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {

            // When
            consumer.subscribe(Collections.singletonList(TOPIC));
            producer1.send(new ProducerRecord<>(TOPIC, "key1", "value1"));
            producer2.send(new ProducerRecord<>(TOPIC, "key2", "value2"));
            producer3.send(new ProducerRecord<>(TOPIC, "key3", "value3"));

            producer1.send(new ProducerRecord<>(TOPIC, "key1", "value4"));
            producer2.send(new ProducerRecord<>(TOPIC, "key2", "value5"));

            // Act
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

            // Assert
            assertEquals(5, records.count());

        }
    }

    @Test
    void testOneProducersSingleConsumerKeyValueMatching() {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);
             KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps)) {


            // When
            consumer.subscribe(Collections.singletonList(TOPIC));
            Map<String, String> expectedRecords = new HashMap<>();
            expectedRecords.put("key1", "value1");
            expectedRecords.put("key2", "value2");

            for (Map.Entry<String, String> entry : expectedRecords.entrySet()) {
                producer.send(new ProducerRecord<>(TOPIC, entry.getKey(), entry.getValue()));
            }

            // Act
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

            // Assert
            assertEquals(expectedRecords.size(), records.count());

            for (ConsumerRecord<String, String> record : records) {
                Assertions.assertTrue(expectedRecords.containsKey(record.key()));
                assertEquals(expectedRecords.get(record.key()), record.value());
            }
        }
    }

}
package org.colak.producer.idempotent;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.colak.producer.util.AdminClientUtil;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Idempotent producer guarantees the order of messages and solves duplication issues
 */
@Slf4j
class IdempotentProducer {

    private static final String TOPIC_NAME = "idempotent_topic";
    private static final String VALUE = "Hello World";

    private KafkaProducer<String, String> kafkaProducer;

    public static void main(String[] args) throws Exception {
        IdempotentProducer producer = new IdempotentProducer();
        producer.produce();
    }

    public void produce() throws ExecutionException, InterruptedException {
        AdminClientUtil.createTopic(TOPIC_NAME, 1, (short) 1);

        kafkaProducer = createProducer();

        sendWithCallback();

        // Tell producer to send all data and block until complete - synchronous
        kafkaProducer.flush();

        // Close the producer
        kafkaProducer.close();
    }

    private KafkaProducer<String, String> createProducer() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // recommended is from https://medium.com/@mbh023/kafka-ordering-message-and-deduplication-with-idempotent-producer-cdc6a3ccc8af
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");

        // max.in.flight.requests.per.connection is for enhancing throughput.
        // Before this option appears, the Kafka producer always has to wait for ACK from the broker. This degrades performance.
        // In case of error all in  flight requests will fail because of idempotence
        properties.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "3");

        retryConfig(properties);

        return new KafkaProducer<>(properties);
    }

    private static void retryConfig(Properties properties) {
        // The retries parameter determines the maximum number of times a producer will attempt to resend a message in case of failure.
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, "1");

        // The retry.backoff.ms parameter determines the time the producer waits between consecutive retries.
        // It specifies the backoff period, allowing the producer to avoid overwhelming the broker with rapid retry attempts.
        //By default, the producer will wait 100ms between retries
        properties.setProperty(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, "1");

        properties.setProperty(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "30000");
    }


    private void sendWithCallback() {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC_NAME, VALUE);
        kafkaProducer.send(producerRecord, (recordMetadata, exception) -> {
            // Executes every time a record is successfully sent or an exception is thrown
            if (exception == null) {
                // The record was successfully sent
                log.info("Received new metadata. \n" +
                         "Topic:" + recordMetadata.topic() + "\n" +
                         "Partition:" + recordMetadata.partition() + "\n" +
                         "Offset:" + recordMetadata.offset() + "\n" +
                         "Timestamp:" + recordMetadata.timestamp());
            } else {
                log.error("Error while producing", exception);
            }
        });
    }
}

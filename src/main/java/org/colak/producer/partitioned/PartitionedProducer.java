package org.colak.producer.partitioned;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Collections;
import java.util.Properties;

/**
 * If you run this code a couple of times partition will be different for each run.
 */
@Slf4j
public class PartitionedProducer {

    private static final String TOPIC_NAME = "partitioned_topic";
    private static final String VALUE = "Hello World";

    private KafkaProducer<String, String> kafkaProducer;

    public static void main(String[] args) {
        PartitionedProducer producer = new PartitionedProducer();
        producer.produce();
    }

    public void produce() {
        createTopic();
        kafkaProducer = createProducer();

        sendWithCallback();

        // Tell producer to send all data and block until complete - synchronous
        kafkaProducer.flush();

        // Close the producer
        kafkaProducer.close();
    }

    private void createTopic() {
        Properties adminProperties = new Properties();
        adminProperties.setProperty("bootstrap.servers", "localhost:9092");
        try (AdminClient adminClient = AdminClient.create(adminProperties)) {
            // Specify the topic name and number of partitions
            int numPartitions = 2;

            // Create a NewTopic instance
            NewTopic newTopic = new NewTopic(TOPIC_NAME, numPartitions, (short) 1); // (short) 1 is the replication factor

            // Create the topic using the AdminClient
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();

            log.info("Topic '" + TOPIC_NAME + "' created with " + numPartitions + " partitions.");
        } catch (Exception exception) {
            log.error("Exception caught", exception);
        }
    }

    private KafkaProducer<String, String> createProducer() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return new KafkaProducer<>(properties);
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

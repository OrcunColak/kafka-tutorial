package org.colak.producer.partitioned.nonsticky;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.serialization.StringSerializer;
import org.colak.producer.util.AdminClientUtil;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/**
 * Even if the producer is rapidly sending it avoids batching because of round-robin
 */
@Slf4j
class NonStickyPartitionedProducerWithoutKey {

    // cd /opt/landoop/kafka/bin
    // kafka-topics --delete --bootstrap-server localhost:9092 --topic non_sticky_partitioned_topic_without_key
    private static final String TOPIC_NAME = "non_sticky_partitioned_topic_without_key";
    private static final String VALUE = "Hello World";

    private KafkaProducer<String, String> kafkaProducer;

    public static void main(String[] args) throws Exception {
        NonStickyPartitionedProducerWithoutKey producer = new NonStickyPartitionedProducerWithoutKey();
        producer.produce();
    }

    private void produce() throws InterruptedException, ExecutionException {
        AdminClientUtil.createTopic(TOPIC_NAME);

        try {
            kafkaProducer = createProducer();

            send();

            // Tell producer to send all data and block until complete - synchronous
            kafkaProducer.flush();
        } finally {
            // Close the producer
            kafkaProducer.close();
        }
    }


    private KafkaProducer<String, String> createProducer() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // If RoundRobinPartitioner is not used, all records go to the same partition
        // If RoundRobinPartitioner is used, records are distributed among partitions
        properties.setProperty(ProducerConfig.PARTITIONER_CLASS_CONFIG, RoundRobinPartitioner.class.getName());

        return new KafkaProducer<>(properties);
    }

    private void send() {
        for (int i = 0; i < 10; i++) {
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
}

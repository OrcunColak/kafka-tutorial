package org.colak.producer.partitioned.nonsticky;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.serialization.StringSerializer;
import org.colak.producer.util.AdminClientUtil;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Even if the producer is rapidly sending it avoids batching because of round-robin
 */
@Slf4j
public class NonStickyPartitionedProducerWithKey {

    // cd /opt/landoop/kafka/bin
    // kafka-topics --delete --bootstrap-server localhost:9092 --topic non_sticky_partitioned_topic_with_key
    private static final String TOPIC_NAME = "non_sticky_partitioned_topic_with_key";
    private static final String VALUE = "Hello World";

    private KafkaProducer<String, String> kafkaProducer;

    public static void main(String[] args) throws Exception {
        NonStickyPartitionedProducerWithKey producer = new NonStickyPartitionedProducerWithKey();
        producer.produce();
    }

    public void produce() throws InterruptedException {
        AdminClientUtil.deleteTopic(TOPIC_NAME);
        AdminClientUtil.createTopic(TOPIC_NAME);
        kafkaProducer = createProducer();

        send();

        // Tell producer to send all data and block until complete - synchronous
        kafkaProducer.flush();

        // Close the producer
        kafkaProducer.close();
    }

    private KafkaProducer<String, String> createProducer() {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer", StringSerializer.class.getName());
        // If RoundRobinPartitioner is not used, all records go to the same partition
        // If RoundRobinPartitioner is used, records are distributed among partitions.
        // However, it seems key is not taken into account
        properties.setProperty("partitioner.class", RoundRobinPartitioner.class.getName());

        return new KafkaProducer<>(properties);
    }

    private void send() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 10; j++) {
                String key = "id_" + i;
                String message = VALUE + j;
                ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC_NAME, key, message);

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
        TimeUnit.SECONDS.sleep(1);
    }
}

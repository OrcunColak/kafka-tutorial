package org.colak.producer.transactional;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

class TransactionalKafkaProducer {

    private static final String TOPIC_NAME = "my_topic";

    KafkaProducer<String, String> kafkaProducer;

    public static void main(String[] args) {
        TransactionalKafkaProducer producer = new TransactionalKafkaProducer();
        producer.produce();
    }

    private void produce() {
        kafkaProducer = createProducer();

        // Initialize transaction
        kafkaProducer.initTransactions();

        try {
            kafkaProducer.beginTransaction();

            // Produce multiple records as part of the transaction
            kafkaProducer.send(new ProducerRecord<>(TOPIC_NAME, "key1", "value1"));
            kafkaProducer.send(new ProducerRecord<>(TOPIC_NAME, "key2", "value2"));
            kafkaProducer.send(new ProducerRecord<>(TOPIC_NAME, "key3", "value3"));

            // Commit the transaction
            kafkaProducer.commitTransaction();
            System.out.println("Transaction committed successfully.");

        } catch (Exception e) {
            System.err.println("Transaction failed, aborting...");
            kafkaProducer.abortTransaction(); // Abort the transaction if any send fails
            e.printStackTrace();
        } finally {
            kafkaProducer.close();
        }

    }

    private KafkaProducer<String, String> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");

        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "my-transactional-id"); // unique transactional ID

        return new KafkaProducer<>(props);
    }
}


package org.colak;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class Producer {

    private static final String TOPIC_NAME = "demo_topic";

    public void produce() {
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "localhost:9092");
        properties.setProperty("key.serializer", StringSerializer.class.getName());
        properties.setProperty("value.serializer", StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        sendWithKey(producer);
        sendWithoutKey(producer);

        // Tell producer to send all data and block until complete - synchronous
        producer.flush();

        // Close the producer
        producer.close();
    }

    private void sendWithKey(KafkaProducer<String, String> producer) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC_NAME,
                "key1",
                "Hello World");
        producer.send(producerRecord);
    }

    private void sendWithoutKey(KafkaProducer<String, String> producer) {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC_NAME,
                "Hello World");
        producer.send(producerRecord);
    }
}

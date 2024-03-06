package org.colak.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

@Slf4j
class ProducerJaas {

    private static final String TOPIC_NAME = "demo_topic";
    private static final String VALUE = "Hello World";

    private KafkaProducer<String, String> kafkaProducer;

    public static void main(String[] args) {
        ProducerJaas producer = new ProducerJaas();
        producer.produce();
    }

    public void produce() {
        kafkaProducer = createProducer();

        sendWithKey();

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

        properties.put("security.protocol", "SASL_SSL");
        properties.put("sasl.mechanism", "PLAIN");

        String format = String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
                "admin", "admin123");

        properties.put("sasl.jaas.config", format);

        return new KafkaProducer<>(properties);
    }

    private void sendWithKey() {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TOPIC_NAME, "key1",
                VALUE);
        kafkaProducer.send(producerRecord);
    }


}

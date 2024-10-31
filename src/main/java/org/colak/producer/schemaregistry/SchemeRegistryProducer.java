package org.colak.producer.schemaregistry;

import io.confluent.kafka.serializers.KafkaAvroSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.colak.demo.MyOrder;

import java.util.Properties;

@Slf4j
class SchemeRegistryProducer {

    private static final String TOPIC_NAME = "order_topic";
    private KafkaProducer<String, MyOrder> kafkaProducer;

    public static void main(String[] args) {
        SchemeRegistryProducer producer = new SchemeRegistryProducer();
        producer.produce();
    }

    private void produce() {
        try {
            kafkaProducer = createProducer();

            sendOrder();

            // Tell producer to send all data and block until complete - synchronous
            kafkaProducer.flush();
        } finally {
            // Close the producer
            kafkaProducer.close();
        }
    }

    private KafkaProducer<String, MyOrder> createProducer() {
        Properties properties = new Properties();
        // 19092 not 9092
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // The KafkaAvroSerializer class is responsible for serializing the message in to Avro format.
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        // To se the schema http://localhost:8081/subjects/order_topic-value/versions/latest
        properties.setProperty("schema.registry.url", "http://localhost:8081");

        return new KafkaProducer<>(properties);
    }

    private void sendOrder() {
        MyOrder order = MyOrder.newBuilder()
                .setOrderId("OId234")
                .setCustomerId("CId432")
                .setSupplierId("SId543")
                .setItems(4)
                .setFirstName("Sunil")
                .setLastName("V")
                .setPrice(178f)
                .setWeight(75f)
                .build();

        ProducerRecord<String, MyOrder> producerRecord = new ProducerRecord<>(TOPIC_NAME, order);
        kafkaProducer.send(producerRecord, (metadata, exception) -> {
            if (exception == null) {
                log.info("Metadata : {}", metadata);
            } else {
                log.error("Exception caught ", exception);
            }
        });
    }

}

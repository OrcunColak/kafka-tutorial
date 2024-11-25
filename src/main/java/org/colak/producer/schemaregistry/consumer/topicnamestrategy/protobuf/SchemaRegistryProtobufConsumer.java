package org.colak.producer.schemaregistry.consumer.topicnamestrategy.protobuf;

import io.confluent.kafka.serializers.subject.TopicNameStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;
import java.util.UUID;

// See https://medium.com/@bryan_56456/schemas-for-kafka-topics-multiple-schemas-vs-composite-schemas-comprehensive-comparison-b34880e81987
// Define a single schema for the topic

// Original protobuf
// message OrderEvent {
//   required uint64 timestamp = 1;
//   required id.Order order = 2;
//
//   oneof event {
//     event.OrderConfirmed confirmed = 10;
//     event.OrderShipped shipped = 11;
//     event.OrderDelivered delivered = 12;
//   }
// }

// New protobuf
// message OrderEvent {
//   required uint64 timestamp = 1;
//   required id.Order order = 2;
//
//   oneof event {
//     event.OrderConfirmed confirmed = 10;
//     event.OrderShipped shipped = 11;
//     event.OrderDelivered delivered = 12;
//     event.OrderPaid paid = 13;
//   }
// }
@Slf4j
class SchemaRegistryProtobufConsumer {


    public static Properties getPropertiesSingleSchema(String bootstrapServers, String schemaRegistryUrl) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServers);
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put("value.subject.name.strategy", TopicNameStrategy.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaProtobufDeserializer.class.getName());
        props.put("specific.protobuf.value.type", OrderEvent.class.getName());

        props.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return props;
    }



}

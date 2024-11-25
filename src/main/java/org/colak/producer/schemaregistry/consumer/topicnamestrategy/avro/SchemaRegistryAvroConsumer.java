package org.colak.producer.schemaregistry.consumer.topicnamestrategy.avro;

import lombok.extern.slf4j.Slf4j;

// See https://medium.com/@bryan_56456/schemas-for-kafka-topics-multiple-schemas-vs-composite-schemas-comprehensive-comparison-b34880e81987
// Define a single schema for the topic

// Original avro
// record OrderEvent {
//     long timestamp;
//     id.Order order;
//     union {
//        event.OrderConfirmed,
//        event.OrderDelivered,
//        event.OrderShipped
//     } event;
// }

// New avro
// record Unknown {}
//
// record OrderEvent {
//     long timestamp;
//     id.Order order;
//     union {
//       event.Unknown,
//       event.OrderConfirmed,
//       event.OrderDelivered,
//       event.OrderShipped,
//       event.OrderPaid
//     } event;
// }
@Slf4j
class SchemaRegistryAvroConsumer {




}

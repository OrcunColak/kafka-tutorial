package org.colak.producer.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Slf4j
@UtilityClass
public class AdminClientUtil {

    public void createTopic(String topicName) throws ExecutionException, InterruptedException {
        createTopic(topicName, 3, (short) 1);
    }

    public void createTopic(String topicName, int numPartitions, short replicationFactor)
            throws ExecutionException, InterruptedException {
        Properties adminProperties = new Properties();
        adminProperties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        try (AdminClient adminClient = AdminClient.create(adminProperties)) {

            // Create a NewTopic instance
            NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);

            // Create the topic using the AdminClient
            Set<NewTopic> topics = Collections.singleton(newTopic);
            CreateTopicsResult createTopicsResult = adminClient.createTopics(topics);
            KafkaFuture<Void> future = createTopicsResult.all();
            future.get();

            log.info("Topic '" + topicName + "' created with " + numPartitions + " partitions.");
        }
    }

    public void deleteTopic(String topicName) throws ExecutionException, InterruptedException {
        Properties adminProperties = new Properties();
        adminProperties.setProperty(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        try (AdminClient adminClient = AdminClient.create(adminProperties)) {

            Set<String> topics = Collections.singleton(topicName);
            DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(topics);
            KafkaFuture<Void> future = deleteTopicsResult.all();
            future.get();

            log.info("Topic '" + topicName + "' is deleted ");
        }
    }
}

# Read me

The original idea is from  
https://medium.com/@mbanaee61/ksqldb-real-time-stream-processing-with-reactive-spring-boot-8af689631fc7

# Create Kafka Topic

kafka-topics.sh --create --topic user_activity --bootstrap-server localhost:19092 --partitions 1 --replication-factor 1

# Produce Sample Messages to the Topic

kafka-console-producer.sh --topic user_activity --bootstrap-server localhost:19092 --property "parse.key=true"
--property "key.separator=:"
user1:{"user_id":"user1","activity":"login","timestamp":1}
user1:{"user_id":"user1","activity":"click","timestamp":2}
user2:{"user_id":"user2","activity":"login","timestamp":3}
user1:{"user_id":"user1","activity":"logout","timestamp":4}
user2:{"user_id":"user2","activity":"click","timestamp":5}

# Create the Stream in ksqlDB

```
CREATE STREAM user_activity (
user_id VARCHAR,
activity VARCHAR,
timestamp BIGINT
) WITH (
KAFKA_TOPIC='user_activity',
VALUE_FORMAT='JSON'
);
```

# Create the Summary Table

Now that the stream is created, you can create a table that summarizes the user activities. Execute the following SQL
statement in the ksqlDB CLI:

```
CREATE TABLE user_activity_summary AS
SELECT user_id, COUNT(activity) AS activity_count
FROM user_activity
WINDOW TUMBLING (SIZE 1 HOUR)
GROUP BY user_id;
```

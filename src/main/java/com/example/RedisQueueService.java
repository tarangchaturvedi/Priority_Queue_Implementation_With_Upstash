package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class RedisQueueService implements QueueService {

    private final Jedis jedis;
    private final Integer visibilityTimeout;

    public RedisQueueService() {
        this.jedis = new Jedis("usable-magpie-43846.upstash.io", 6379, true);
        this.jedis.auth("AatGAAIjcDEwMThkZTJiOGIxODM0YTA5YTYxNDkyMDRkNWI1NjZkNnAxMA");

        String propFileName = "config.properties";
        Properties confInfo = new Properties();
        try (InputStream inStream = getClass().getClassLoader().getResourceAsStream(propFileName)) {
            confInfo.load(inStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.visibilityTimeout = Integer.parseInt(confInfo.getProperty("visibilityTimeout", "30"));
    }
    @Override
    public void push(String queueUrl, String message, int priority) {
        try {
            System.out.println("Pushing message: " + message + " with priority: " + priority);
            Message msg = new Message(message, priority, System.currentTimeMillis());
            msg.setReceiptId(UUID.randomUUID().toString());
            // System.out.println("Serialized Message: " + new Gson().toJson(msg));
    
            this.jedis.zadd(queueUrl, score(msg), new Gson().toJson(msg)); // Push to Redis using the ZADD command.
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public Message pull(String queueUrl, boolean toremove) {
        
        Long nowTime = now();
        try {
            Set<Tuple> tuples = this.jedis.zrangeWithScores(queueUrl, 0, 0); // To fetch the message with the lowest score (highest priority)
            for (Tuple tuple : tuples) {
                String deserializedMessage = tuple.getElement();
                // System.out.println("deserializedMessage: " + deserializedMessage);
                Message msg = new Gson().fromJson(deserializedMessage, Message.class);
                
                if (msg != null) {
                    // msg.setReceiptId(UUID.randomUUID().toString()); // Set a new receipt ID and increment attempt count and visibility timeout.
                    msg.incrementAttempts();
                    msg.setVisibleFrom(nowTime + TimeUnit.SECONDS.toMillis(visibilityTimeout));
                    if (toremove){
                        this.jedis.zrem(queueUrl, deserializedMessage);
                    }
                    
                    return new Message(msg.getBody(), msg.getReceiptId()); // Return the message with the receipt ID
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void delete(String queueUrl, String receiptId) {
        try {
            Set<String> messages = jedis.zrange(queueUrl, 0, -1);  // Fetch all messages from the sorted set.
    
            for (String serializedMessage : messages) {
                Message msg = new Gson().fromJson(serializedMessage, Message.class);
                
                if (msg.getReceiptId().equals(receiptId)) {
                    jedis.zrem(queueUrl, serializedMessage); // Remove the message from the sorted set using ZREM Command.
                    System.out.println("Deleted message with receiptId: " + receiptId);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    long now() {
        return System.currentTimeMillis();
    }

    private double score(Message message) { // The score is based on the priority and timestamp, Highest Priority message will have Lowest Score.
        double messageScore = -(double) message.getPriority() + (double) message.getTimestamp() / 1e12;
        return messageScore;
        
    }
}

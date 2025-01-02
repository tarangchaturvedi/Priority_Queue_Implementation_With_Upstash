package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.*;

public class InMemoryPriorityQueueService implements QueueService {
    private final Map<String, PriorityBlockingQueue<Message>> queues;
    protected final long visibilityTimeout;

    InMemoryPriorityQueueService() {
        this.queues = new ConcurrentHashMap<>();
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
    public void push(String queueUrl, String msgBody, int priority) {
        System.out.println("Pushing message: " + msgBody + " with priority: " + priority);
        PriorityBlockingQueue<Message> queue = queues.get(queueUrl);
        if (queue == null) {
            queue = new PriorityBlockingQueue<>();
            queues.put(queueUrl, queue);
        }

        long nowTime = now();
        Message newmessage = new Message(msgBody, priority, System.nanoTime());
        // newmessage.setReceiptId(UUID.randomUUID().toString());
        queue.add(newmessage);
    }

    @Override
    public Message pull(String queueUrl, boolean toremove) {
        PriorityBlockingQueue<Message> queue = queues.get(queueUrl);
        if (queue == null) { // return Null if EptyQueue.
            return null;
        }
        long nowTime = now();
        Message msg = null;

        // Find the highest priority message.
        for (Iterator<Message> it = queue.iterator(); it.hasNext(); ) {
            msg = it.next();
            
            if (msg != null && msg.isVisibleAt(nowTime)) {
                if (toremove){
                    it.remove();  // Remove it from the queue once pulled.
                }

                msg.setReceiptId(UUID.randomUUID().toString()); // Set a new receipt ID and increment attempt count and visibility timeout.
                msg.incrementAttempts();
                
                msg.setVisibleFrom(System.nanoTime() + TimeUnit.SECONDS.toNanos(visibilityTimeout));
                // msg.setVisibleFrom(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(visibilityTimeout));
                System.out.println("Pulling mesggae: " + msg);
                return new Message(msg.getBody(), msg.getReceiptId()); // Return message with the body and receipt ID
            }
        }

        return null;
    }

    @Override
    public void delete(String queueUrl, String receiptId) {
        PriorityBlockingQueue<Message> queue = queues.get(queueUrl);
        if (queue != null) {
            long nowTime = now();
            for (Iterator<Message> it = queue.iterator(); it.hasNext(); ) {
                Message msg = it.next();
                if (msg.getReceiptId().equals(receiptId)) {
                    it.remove();
                    System.out.println("Deleted message with receiptId: " + receiptId);
                    break;
                }
            }
        }
    }

    long now() {
        return System.nanoTime(); // NanoTime is used as two messages with same priority can be enqueued at same MilliSecond time.
    }
}

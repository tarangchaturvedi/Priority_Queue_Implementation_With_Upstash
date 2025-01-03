package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

        Message newmessage = new Message(msgBody, priority, System.nanoTime());
        // newmessage.setReceiptId(UUID.randomUUID().toString());
        queue.add(newmessage);
    }

    @Override
    public Message pull(String queueUrl) {
        PriorityBlockingQueue<Message> queue = queues.get(queueUrl);
        if (queue == null) {
            return null;
        }
        long nowTime = now();
        Message msg;
        List<Message> temparray = new ArrayList<>(); // Temporary list to store non-visible messages
        
        while ((msg = queue.poll()) != null) { // Poll messages and check visibility
            if (msg.isVisibleAt(nowTime)) {
                msg.setReceiptId(UUID.randomUUID().toString());
                msg.incrementAttempts();
                msg.setVisibleFrom(System.nanoTime() + TimeUnit.SECONDS.toNanos(visibilityTimeout));

                System.out.println("Pulled message: " + msg);
                queue.add(msg);
                return new Message(msg.getBody(), msg.getReceiptId());
            }
            temparray.add(msg);
        }

        for (Message message : temparray) { // After processing all visible messages, re-add non-visible messages to the queue
            queue.add(message);
        }
        return null; // Return null if no visible message was found
    }


    @Override
    public void delete(String queueUrl, String receiptId) {
        PriorityBlockingQueue<Message> queue = queues.get(queueUrl);
        if (queue != null) {
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

    @Override
    public void clearQueue(String queueUrl) {
        queues.remove(queueUrl);        
    }
}

// PriorityBlockingQueue<Message> queue = new PriorityBlockingQueue<>(11, new Comparator<Message>() {
//     @Override
//     public int compare(Message m1, Message m2) {
//         // Higher priority messages come first
//         int priorityComparison = Integer.compare(m2.getPriority(), m1.getPriority());
//         if (priorityComparison == 0) {
//             // If priorities are the same, use timestamp (FIFO for equal priority)
//             return Long.compare(m1.getTimestamp(), m2.getTimestamp());
//         }
//         return priorityComparison;
//     }
// });

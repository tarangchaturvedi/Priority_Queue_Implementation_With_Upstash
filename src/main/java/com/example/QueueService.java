package com.example;

public interface QueueService {
  /** push a message onto a queue. */
  public void push(String queueUrl, String messageBody, int priority);

  /** retrieves a single message from a queue. */
  public Message pull(String queueUrl); // toremove variable ensures if retrieved message is to be removed from queue or not.

  /** deletes a message from the queue that was received by pull(). */
  public void delete(String queueUrl, String receiptId);
  
  /* Clear Queue */
  public void clearQueue(String queueUrl);
  
}

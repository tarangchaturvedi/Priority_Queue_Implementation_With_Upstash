package com.example;

public class Message implements Comparable<Message>{

  private int attempts; // How many times this message has been delivered.

  private long visibleFrom; // Visible from time.

  private String receiptId; // An identifier associated with the act of receiving the message.

  private int priority; //priority field for message.
  private long timestamp; //Time of pushing message.

  private String msgBody;

  Message(String msgBody, int priority, long timestamp) {
    this.msgBody = msgBody;
    this.priority = priority;
    this.timestamp = timestamp;
  }

  Message(String msgBody, String receiptId) {
    this.msgBody = msgBody;
    this.receiptId = receiptId;
  }

  public String getReceiptId() {
    return this.receiptId;
  }

  public int getPriority() { // new method to return priority.
    return this.priority;
  }

  public long getTimestamp() { // new method to return timestamp.
    return this.timestamp;
  }

  protected void setReceiptId(String receiptId) {
    this.receiptId = receiptId;
  }

  protected void setVisibleFrom(long visibleFrom) {
    this.visibleFrom = visibleFrom;
  }

  /*
  public boolean isVisible() {
  	return visibleFrom < System.currentTimeMillis();
  }*/

  public long getVisibleFrom() { // new method to return visible form
    return this.visibleFrom;
  }

  public boolean isVisibleAt(long instant) {
    return this.visibleFrom < instant;
  }

  public String getBody() {
    return this.msgBody;
  }

  protected int getAttempts() {
    return this.attempts;
  }

  protected void incrementAttempts() {
    this.attempts++;
  }

  public void setPriority(int priority) {
      this.priority = priority;
  }

  public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
  }

  @Override
  public String toString() {
      return "Message{body='" + msgBody + "', priority=" + priority + ", timestamp=" + timestamp + "}";
  }

  @Override
  public int compareTo(Message other) { // method to compare two messages. returns the message with higher priority and if that is same returns on FCFS basis.

      int rankComparison = Integer.compare(other.getPriority(), this.priority);
      if (rankComparison == 0) {
          return Long.compare(this.timestamp, other.timestamp);
      }
      return rankComparison;
  }
}

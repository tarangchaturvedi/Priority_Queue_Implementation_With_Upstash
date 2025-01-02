package com.example;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class UpstashRedisQueueTest {

    private QueueService qs;
    private String queueUrl = "testRedisQueue";

    @Before
    public void setup() {
        qs = new RedisQueueService(); // Using the RedisQueueService object
    }

    @Test
    public void testSendMessage() {
        qs.push(queueUrl, "Good message!", 1);
        Message msg = qs.pull(queueUrl, true);

        assertNotNull(msg);
        assertEquals("Good message!", msg.getBody());
    }

    @Test
    public void testPullMessage() {
        String msgBody1 = "tarang_chaturvedi First";
        String msgBody2 = "tarang_chaturvedi Second";
        String msgBody3 = "tarang_chaturvedi Third";

        qs.push(queueUrl, msgBody1, 2);
        qs.push(queueUrl, msgBody2, 3);
        qs.push(queueUrl, msgBody3, 1);

        Message msg1 = qs.pull(queueUrl, true);
        Message msg2 = qs.pull(queueUrl, true);
        Message msg3= qs.pull(queueUrl, true);
        
        // pulled messages should have order msgBody2(priority=3) ---> msgBody1(priority=2) ---> msgBody3(priority=1)
        
        assertTrue(msgBody1.equals(msg2.getBody()) && msgBody2.equals(msg1.getBody()) && msgBody3.equals(msg3.getBody()));
    }

    @Test
    public void testPullEmptyQueue() {
        // Empty Queue should return Null.
        Message msg = qs.pull(queueUrl, true);
        assertNull(msg);
    }

    @Test
    public void testDoublePull() {
        qs.push(queueUrl, "Message A.", 1);
        qs.pull(queueUrl, true);
        Message msg = qs.pull(queueUrl, true); //Another pull should return Null.
        assertNull(msg);
    }

    @Test
    public void testDeleteFromQueue() {
        String msgBody = "Message to be deleted.";
        qs.push(queueUrl, msgBody, 1);

        Message msg = qs.pull(queueUrl, false); //only retrieving the receiptID.
        qs.delete(queueUrl, msg.getReceiptId());
        // msg = qs.pull(queueUrl, true);
        // assertNull(msg);
    }

    @Test
    public void testFCFSWithPriority() {
        String[] msgStrs = { "Test msg 1", "Test msg 2", "Test msg 3" };

        qs.push(queueUrl, msgStrs[0], 8);
        qs.push(queueUrl, msgStrs[1],6); 
        qs.push(queueUrl, msgStrs[2], 6);
        // Highest priority message should be first followed by pulling on FCFS basis.

        Message msg1 = qs.pull(queueUrl, true);
        Message msg2 = qs.pull(queueUrl, true);
        Message msg3 = qs.pull(queueUrl, true);

        assertTrue(msgStrs[0].equals(msg1.getBody()) && msgStrs[1].equals(msg2.getBody()) && msgStrs[2].equals(msg3.getBody()));
    }

    @Test
	public void testAckTimeout(){
		RedisQueueService queueService = new RedisQueueService() {
            @Override
			long now() {
				// return System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(this.visibilityTimeout) + 1;
                return System.nanoTime() + TimeUnit.SECONDS.toNanos(this.visibilityTimeout) + 1;
			}
		};
		
        String msgbody = "Check Message";

		queueService.push(queueUrl, msgbody, 1);
		queueService.pull(queueUrl,false);
		Message msg = queueService.pull(queueUrl, true);
		
        assertTrue(msg != null && msgbody.equals(msg.getBody()));
	}

}

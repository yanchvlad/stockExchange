

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

public class OrderConsumerTest {
    LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

    @AfterEach
    public void clean() {
        queue.clear();
    }

    @Test
    public void test_Success() throws IOException, InterruptedException {
        OrderBook orderBook = new OrderBook();
        Thread orderConsumer = new Thread(new OrderConsumer(orderBook, queue));
        orderConsumer.start();
        String consoleString = "B,100345,5103,1000,10000";
        queue.add(consoleString);
        Thread.sleep(1000);
        assertTrue(queue.isEmpty());
        assertEquals(orderBook.getOrdersToBuy(), "[BUY,100345,5103$,1000/10000]");
    }

    @Test
    public void testMultiThreading_Success() throws IOException, InterruptedException {
        OrderBook orderBook = new OrderBook();
        Thread orderConsumer1 = new Thread(new OrderConsumer(orderBook, queue));
        Thread orderConsumer2 = new Thread(new OrderConsumer(orderBook, queue));
        Thread orderConsumer3 = new Thread(new OrderConsumer(orderBook, queue));
        orderConsumer1.start();
        orderConsumer2.start();
        orderConsumer3.start();
        queue.add("B,1,1,10,10");
        queue.add("B,2,2,1000,20");
        queue.add("S,22,2,100");
        // need in a process to detect inconsistent state of orderBook
        Thread.sleep(500);
        queue.add("S,23,1,100");
        Thread.sleep(500);
        queue.add("S,24,1,109");
        Thread.sleep(500);
        queue.add("S,21,1,9");
        Thread.sleep(500);
        queue.add("B,3,1,30");
        Thread.sleep(500);
        queue.add("S,25,4,109");
        Thread.sleep(500);
        queue.add("B,4,2,1");
        Thread.sleep(500);
        queue.add("S,26,1,1009");
        Thread.sleep(500);
        queue.add("B,5,3,10");
        Thread.sleep(500);
        queue.add("S,27,1,1100,100");
        Thread.sleep(500);
        queue.add("B,6,4,5000,5");
        Thread.sleep(1000);
        // buy (10 + 1000 + 30 + 1 + 10 + 5000) - sell (100 + 100 + 109 + 9 + 109 + 1009 + 1100)
        assertTrue(queue.isEmpty());
        System.out.println(orderBook.getOrdersToSell());
        System.out.println(orderBook.getOrdersToBuy());
        assertEquals(orderBook.getOrdersToSell(), "[]");
        assertEquals(orderBook.getOrdersToBuy(), "[BUY,6,4$,3515/5]");
    }

    @Test
    public void testOrderProducer_incorrectOrder() throws IOException, InterruptedException {
        OrderBook orderBook = new OrderBook();
        Thread orderConsumer = new Thread(new OrderConsumer(orderBook, queue));
        orderConsumer.start();
        queue.add("B,100345,5103,1000,10000");
        queue.add("S,100346,5103,10000");
        Thread.sleep(2000);
        assertTrue(queue.isEmpty());
        assertEquals(orderBook.getOrdersToBuy(), "[]");
    }

}

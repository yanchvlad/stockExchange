package org.finances;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

public class OrderConsumerTest {
    OrderBook orderBook = new OrderBook();
    LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

    @AfterEach
    public void clean() {
        queue.clear();
    }

    @Test
    public void testOrderProducer_Success() throws IOException, InterruptedException {
        Thread orderConsumer = new Thread(new OrderConsumer(orderBook, queue));
        orderConsumer.start();
        String consoleString = "B,100345,5103,1000,10000";
        queue.add(consoleString);
        Thread.sleep(2000);
        orderConsumer.interrupt();
        // TODO: check that order book contains orders, and queu is empty
    }

    @Test
    public void testOrderProducer_incorrectOrder() throws IOException, InterruptedException {
        Thread orderConsumer = new Thread(new OrderConsumer(orderBook, queue));
        orderConsumer.start();
        queue.add("B,100345,5103,1000,10000");
        queue.add("S,100346,5103,10000");
        Thread.sleep(2000);
        orderConsumer.interrupt();
        // TODO: check that order book contains orders, and queu is empty
    }

}

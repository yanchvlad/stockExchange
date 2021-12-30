package org.finances;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class OrderConsumer implements Runnable {

    OrderBook orderBook;
    LinkedBlockingQueue<String> queue;

    public OrderConsumer(OrderBook orderBook, LinkedBlockingQueue<String> queue) {
        this.orderBook = orderBook;
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Waits until element will be available in timeout
                String orderString = queue.poll(1, TimeUnit.SECONDS);
                if (Order.isValid(orderString)) {
                    // System.out.println(orderString);
                    orderBook.handle(new Order(orderString));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            } catch (OrderException e) {
                e.printStackTrace();
            }
        }
    }
}

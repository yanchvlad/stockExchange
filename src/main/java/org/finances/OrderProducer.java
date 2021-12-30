package org.finances;

import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;


public class OrderProducer implements Runnable {
    
    OrderBook orderBook;
    LinkedBlockingQueue<String> queue;

    public OrderProducer(OrderBook orderBook, LinkedBlockingQueue<String> queue) {
        this.orderBook = orderBook;
        this.queue = queue;
    }

    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        try {
            while(sc.hasNextLine()) {
                String consoleString = sc.nextLine();
                if (Order.isValid(consoleString)) {
                    queue.add(consoleString);
                } else {
                    System.err.println("Order " + consoleString+ " is invalid");
                }
                
            }
        } finally {
            sc.close();
        }
    }
    
}

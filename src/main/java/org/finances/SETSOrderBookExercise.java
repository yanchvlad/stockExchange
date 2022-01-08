

import java.util.concurrent.LinkedBlockingQueue;

public class SETSOrderBookExercise 
{

    private static final long WAITING_TIME_FOR_CONSUMER = 2000;
    public static void main(String[] args)
    {
        final LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
        // To make its working with custom tests disregarded using Singleton
        // OrderBook orderBook = OrderBook.getInstance();
        OrderBook orderBook = new OrderBook();

        Thread orderProducer = new Thread(new OrderProducer(orderBook, queue));
        orderProducer.start();

        Thread orderConsumer = new Thread(new OrderConsumer(orderBook, queue));
        orderConsumer.start();
        wait(queue);
    }

    private static void wait(LinkedBlockingQueue<String> queue) {
        boolean finished = false;
        while (true) {
            if (queue.isEmpty()) {
                try {
                    Thread.sleep(WAITING_TIME_FOR_CONSUMER);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finished = true;
            }
            if (finished) {
                if (queue.isEmpty()) {
                    return;
                }
                finished = false;
            }
        }
    }
}

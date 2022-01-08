

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

public class OrderProducerTest {
    OrderBook orderBook = new OrderBook();
    LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();

    @AfterEach
    public void clean() {
        queue.clear();
    }

    @Test
    public void testOrderProducer_Success() throws IOException, InterruptedException {
        OrderProducer producer = new OrderProducer(orderBook, queue);     
        String consoleString = "B,100345,5103,1000,10000";
        putInput(consoleString, producer);
        String orderString = queue.poll(1, TimeUnit.SECONDS);
        assertEquals(orderString, consoleString);
    }

    @Test
    public void testOrderProducer_incorrectOrder() throws IOException, InterruptedException {
        OrderProducer producer = new OrderProducer(orderBook, queue);     
        String consoleString = "AAPL 100";
        putInput(consoleString, producer);
        String orderString = queue.poll(1, TimeUnit.SECONDS);
        assertEquals(orderString, null);
    }

    private void putInput(String input, OrderProducer producer) throws IOException {
        InputStream oldIn = System.in;

        try (
            ByteArrayInputStream newIn = new ByteArrayInputStream(input.getBytes());
        ){
            System.setIn(newIn);        
            producer.run();
        } finally {
            System.setIn(oldIn);
        }
    }
}

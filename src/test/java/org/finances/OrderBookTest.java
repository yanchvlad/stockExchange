

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class OrderBookTest {

    @Test
    public void testHandledOrders_CheckOrdersLists() throws OrderNotHandledException, OrderException {
        OrderBook orderBook = new OrderBook();
        orderBook.handle(new Order("S,1,2,12"));
        assertEquals(orderBook.getOrdersToSell(), "[SELL,1,2$,12/null]");
        orderBook.handle(new Order("B,2,1,10000,100"));
        assertEquals(orderBook.getOrdersToSell(), "[SELL,1,2$,12/null]");
        assertEquals(orderBook.getOrdersToBuy(), "[BUY,2,1$,10000/100]");
        orderBook.handle(new Order("B,3,2,1000,100"));
        assertEquals(orderBook.getOrdersToSell(), "[]");
        assertEquals(orderBook.getOrdersToBuy(), "[BUY,3,2$,988/100, BUY,2,1$,10000/100]");

        orderBook.handle(new Order("S,4,2,100"));
        assertEquals(orderBook.getOrdersToSell(), "[]");
        assertEquals(orderBook.getOrdersToBuy(), "[BUY,3,2$,888/100, BUY,2,1$,10000/100]");
        orderBook.handle(new Order("S,5,2,889,100"));
        assertEquals(orderBook.getOrdersToSell(), "[SELL,5,2$,1/100]");
        assertEquals(orderBook.getOrdersToBuy(), "[BUY,2,1$,10000/100]");
    }

}

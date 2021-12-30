package org.finances;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class OrderBookTest {

    @Test
    public void testLockToAllocate_Success() throws OrderNotHandledException, OrderException {
        OrderBook orderBook = new OrderBook();
        orderBook.handle(new Order("B,1,1,100"));
        orderBook.handle(new Order("S,2,2,100"));
        Set<Integer> lockIds = new HashSet<>();
        List<Integer> ordersrIdsToLock = new ArrayList<>();
        ordersrIdsToLock.add(1);
        Object mutex = new Object();
        List<Integer> lockedIds = orderBook.lockToAllocate(mutex, lockIds, ordersrIdsToLock, 101);
        assertEquals(Arrays.asList(1), lockedIds);
    }

    @Test
    public void testHandledOrders_Success() throws OrderNotHandledException, OrderException {
        OrderBook orderBook = new OrderBook();
        orderBook.handle(new Order("B,1,1,10000,100"));
        orderBook.handle(new Order("B,2,1,1000,100"));
        orderBook.handle(new Order("S,3,2,100"));
        orderBook.handle(new Order("S,4,1,100"));
        Set<Integer> lockIds = new HashSet<>();
        List<Integer> ordersrIdsToLock = new ArrayList<>();
        ordersrIdsToLock.add(1);
        ordersrIdsToLock.add(2);
        Object mutex = new Object();
        List<Integer> lockedIds = orderBook.lockToAllocate(mutex, lockIds, ordersrIdsToLock, 10900);
        assertEquals(Arrays.asList(1, 2), lockedIds);
    }


    @Test
    public void testLockToAllocate_Fail() throws OrderException{
        OrderBook orderBook = new OrderBook();
        Set<Integer> lockIds = new HashSet<>();
        List<Integer> ordersrIdsToLock = new ArrayList<>();
        Object mutex = new Object();
        
        try {
            List<Integer> lockedIds = orderBook.lockToAllocate(mutex, lockIds, ordersrIdsToLock, 100);
            fail("Should throw exception");
        } catch (OrderNotHandledException e) {
            assertEquals("There are no existing orders for ids: []", e.getMessage());
        }
    }   
}

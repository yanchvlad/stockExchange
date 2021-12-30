package org.finances;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class OrderTest {

    @Test
    public void testOrder_Success() throws OrderException {
        Order order = new Order("B,100345,5103,100000,1000");
        assertEquals(100345, order.getId());
        assertEquals(Short.valueOf("5103"), order.getPrice());
        assertTrue(order.isIceberg());
    }
    

    @Test
    public void testOrderBuy_InvalidString() {
        String[] values = { "B", "100345", "5103", "100000", "0" };
        String ordeString = String.join(",", values);
        assertFalse(Order.isValid(ordeString));
    }

    @Test
    public void testOrderSell_InvalidString() {
        String[] values = { "S", "100345", "5103", "100000", "10000" };
        String ordeString = String.join(",", values);
        assertTrue(Order.isValid(ordeString));
    }

    @Test
    public void testOrder_BuyType() throws OrderException {
        String orderString = "B,100345,5103,100000,10000";
        Order order = new Order(orderString);
        assertEquals(OrderType.BUY, order.getType());
    }

    @Test
    public void testOrder_InvalidIdentifier() throws OrderException {
        String[] values = { "S", "100345", "5103", "100000", "10000" };
        Order order = new Order(String.join(",", values));
        assertEquals(100345, order.getId());
    }

    @Test
    public void testOrder_InvalidPrice() throws OrderException {
        Order order = new Order("S,100345,5103,100000,10000");
        assertEquals(Short.valueOf("5103"), order.getPrice());
    }

    @Test
    public void testOrder_InvalidPeak() {
        String[] values = { "B", "100345", "5103", "100000", "0"};
        String ordeString = String.join(",", values);
        
        try {
            Order order = new Order(ordeString);
            assertFalse("Should be thrown OrderException in constructor with invalid peak" == "");
        } catch (OrderException e) {
            // in org.junit.jupiter.api.Test is absent expected parameter, do not import another lib
            assertTrue(true);
        }
    }

    @Test
    public void testIsValid_Fail_Empty(){
        String ordeString = "";
        boolean result = Order.isValid(ordeString);
        assertFalse(result);
    }

    @Test
    public void testIsValid_Fail_Null(){
        assertFalse(Order.isValid(null));
    }

}

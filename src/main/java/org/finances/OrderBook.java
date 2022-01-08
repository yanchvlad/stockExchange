

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;

interface OrderComparisonFunction {
    Boolean run(Order order1, Order order2);
}

public class OrderBook {
    String EMPTY_BUY_STRING = String.format("|%10s|%13s|%7s", "", "", "");
    String EMPTY_SELL_STRING = String.format("|%7s|%13s|%10s|", "", "", "");
    Set<Integer> lockedIdsToBuy = new HashSet<>();
    Set<Integer> lockedIdsToSell = new HashSet<>();
    List<Order> ordersToBuy = new ArrayList<>();
    List<Order> ordersToSell = new ArrayList<>();
    Map<Integer, Order> ordersMap = new HashMap<>();
    private Object mutexToPrint = new Object();
    private Object mutexToBuy = new Object();
    private Object mutexToSell = new Object();
    OrderComparisonFunction lComparisonBuy = (existingOrder, order) -> existingOrder.getPrice() >= order.getPrice();
    OrderComparisonFunction lComparisonSell = (existingOrder, order) -> order.getPrice() >= existingOrder.getPrice();

    public OrderBook() {
        super();
    }

    // Short version of sending a copy of list of orders, to avoid possible implementation of changes in object
    public String getOrdersToBuy() {
        return ordersToBuy.toString();
    }

    public String getOrdersToSell() {
        return ordersToSell.toString();
    }

    private List<Order> getOrdersToExecuteOrder(Order order) {
        return order.isBuy() ?  ordersToSell: ordersToBuy;
    }

    private OrderComparisonFunction getOrderComaprison(Order order) {
        return order.isBuy() ? lComparisonBuy: lComparisonSell;
    }

    private Set<Integer>  getLockedIds(Order order) {
        return order.isBuy() ?  lockedIdsToSell: lockedIdsToBuy;
    }

    private Object getOrderMutex(Order order) {
        return order.isBuy() ? mutexToBuy: mutexToSell;
    }

    private void addOrderInList(Order order) {
        List<Order> orders = order.isBuy() ? ordersToBuy: ordersToSell;
        OrderComparisonFunction comparison = getOrderComaprison(order);
        int i =0;
        while (i < orders.size()) {
            if (!comparison.run(orders.get(i), order)) {
                break;
            }
            i += 1;
        }
        orders.add(i, order);
    }

    public void handle(Order order) {
        if (ordersMap.get(order.getId()) != null) {
            System.err.println("Order with the same id already exists: " + order + " vs "+ ordersMap.get(order.getId()));
            return;
        }

        ordersMap.put(order.getId(), order);
        try {
            allocate(order);
        } catch (OrderNotHandledException e) {
            // pass
        }
        synchronized(mutexToPrint) {
            if (!order.isExecuted()) {
                addOrderInList(order);
            }
            printValues();
        }
    }

    private void executeOrders(Order existingOrder, Order newOrder) {
        int quantity = newOrder.execute(existingOrder.getQuantity());
        existingOrder.execute(quantity);

        Order buyOrder = newOrder.isBuy() ? newOrder: existingOrder;
        Order sellOrder = (!newOrder.isBuy()) ? newOrder: existingOrder;
        int price = Math.max(sellOrder.getPrice(), buyOrder.getPrice());
        System.out.println(String.format("%d,%d,%d,%d", buyOrder.getId(), sellOrder.getId(), price, quantity));
    }

    private void allocate(Order order) throws OrderNotHandledException {
        OrderComparisonFunction comparison = getOrderComaprison(order);
        List<Order> existingOrders = getOrdersToExecuteOrder(order);
        Set<Integer> lockedIds = getLockedIds(order);
        Object mutex = getOrderMutex(order);

        // get ids of orders fitted in interests of new order and lock ids with mutex
        List<Integer> newLockedIds = lockToAllocate(mutex, lockedIds, existingOrders, comparison, order);
        if (newLockedIds.isEmpty()) {
            throw new OrderNotHandledException("No matching orders");
        }
        for (Integer lockedId : newLockedIds) {
            Order existingOrder = ordersMap.get(lockedId);
            executeOrders(existingOrder, order);
            releaseLock(mutex, lockedId, lockedIds);
        }
    }

    private void releaseLock(Object mutex, Integer lockedId, Set<Integer> lockedIds) {
        synchronized(mutex) {
            lockedIds.remove(lockedId);
        }
    }
    
    protected List<Integer> lockToAllocate(Object mutex, Set<Integer> lockedIds, 
                                        List<Order> existingOrders, OrderComparisonFunction comparison, Order order) throws OrderNotHandledException {
        int allocatedSum = 0;
        List<Integer> newLockedIds = new ArrayList<>();
        synchronized(mutex) {
            for (Order existingOrder : existingOrders) {
                if (comparison.run(order, existingOrder) && !lockedIds.contains(existingOrder.getId())) {
                    newLockedIds.add(existingOrder.getId());
                    lockedIds.add(existingOrder.getId());
                    allocatedSum += existingOrder.getQuantity();
                    // System.out.println("Allocated " + existingOrder.getQuantity() + " from " + existingOrders);
                    if (allocatedSum >= order.getQuantity()) {
                        break;
                    }
                }
                
            }
        }
        if (allocatedSum == 0) {
            throw new OrderNotHandledException("There are no existing orders for ids: " + existingOrders);
        }
        return newLockedIds;
    }

    public void printValues() {
        StringBuilder output = new StringBuilder();
        output.append("+-----------------------------------------------------------------+\n");
        output.append("| BUY                            | SELL                           |\n");
        output.append("| Id       | Volume      | Price | Price | Volume      | Id       |\n");
        output.append("+----------+-------------+-------+-------+-------------+----------+\n");
        int i = 0;
        while (true) {
            // |         1|            1|      1|       |             |          |
            if (this.ordersToBuy.size() <= i && this.ordersToSell.size() <= i) {
                break;
            }
            String buyOutput = formatOrder(this.ordersToBuy, i, true);
            String sellOutput = formatOrder(this.ordersToSell, i, false);
            if (buyOutput == EMPTY_BUY_STRING && sellOutput == EMPTY_SELL_STRING) {
                break;
            }
            output.append(buyOutput);
            output.append(sellOutput);
            i += 1;
            output.append("\n");
        }
        output.append("+-----------------------------------------------------------------+\n");
        System.out.print(output.toString());
    }


    private String formatOrder(List<Order> orders, int i, boolean isBuy) {
        String output = isBuy? EMPTY_BUY_STRING : EMPTY_SELL_STRING;
        Order order = null;
        while (orders.size() > i) {
            order = orders.get(i);
            // delete order from list if it's Executed
            if (order.isExecuted()) {
                orders.remove(i);
            } else {
                break;
            }
        }
        if (order != null && !order.isExecuted()) {
            output = order.getFormattedOrder();
        }
        return output; 
    }
}

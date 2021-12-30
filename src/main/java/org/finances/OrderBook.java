package org.finances;

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
    private Object mutexToBuy = new Object();
    private Object mutexToSell = new Object();
    OrderComparisonFunction lLessEqual = (existingOrder, order) -> existingOrder.getPrice() >= order.getPrice();
    OrderComparisonFunction lGreaterEqual = (existingOrder, order) -> existingOrder.getPrice() <= order.getPrice();

    public OrderBook() {
        super();
    }

    private List<Order> getOrdersToExecuteOrder(Order order) {
        return order.isBuy() ?  ordersToSell: ordersToBuy;
    }

    private OrderComparisonFunction getOrderComaprison(Order order) {
        return order.isBuy() ?  lLessEqual: lGreaterEqual;
    }

    private Set<Integer>  getLockedIds(Order order) {
        return order.isBuy() ?  lockedIdsToSell: lockedIdsToBuy;
    }

    private Object getOrderMutex(Order order) {
        return order.isBuy() ? mutexToBuy: mutexToSell;
    }

    public void handle(Order order) {
        try {
            // handle case when order is Iceberg and can be executed several times, 
            // if it can't be executed will be raised OrderNotHandledException
            while (order.getQuantity() > 0) {
                allocate(order);
            }
        } catch (OrderNotHandledException e) {
            ordersMap.put(order.getId(), order);
            if (order.isBuy()) {
                ordersToBuy.add(order);
            } else {
                ordersToSell.add(order);
            }
        }
        printValues();
    }

    private void removeFromMapIfEmpty(Order order) {
        if (order.getQuantity() <= 0) {
            if (order.getQuantity() < 0) {
                System.err.println(order.getId() + " is less than 0 " + order.getQuantity());
            }
            this.ordersMap.remove(order.getId());
        }
    }

    private void executeOrders(Order existingOrder, Order newOrder) {
        int quantity = newOrder.execute(existingOrder.getQuantity());
        existingOrder.execute(quantity);

        Order buyOrder = newOrder.isBuy() ? newOrder: existingOrder;
        Order sellOrder = (!newOrder.isBuy()) ? newOrder: existingOrder;
        int price = Math.max(sellOrder.getPrice(), buyOrder.getPrice());
        System.out.println(String.format("%d,%d,%d,%d", buyOrder.getId(), sellOrder.getId(), price, quantity));
        System.err.println(String.format("%d,%d,%d,%d", buyOrder.getId(), sellOrder.getId(), price, quantity));
        removeFromMapIfEmpty(existingOrder);
        removeFromMapIfEmpty(newOrder);
    }

    private void allocate(Order order) throws OrderNotHandledException {
        OrderComparisonFunction comparison = getOrderComaprison(order);
        List<Order> existingOrders = getOrdersToExecuteOrder(order);
        Set<Integer> lockedIds = getLockedIds(order);
        Object mutex = getOrderMutex(order);

        // get ids of orders fittied of interests of new order
        List<Integer> orderIdsAvailableToLock = getOrderIdsAvailableToLock(order, existingOrders, comparison);
        // lock ids with mutex
        List<Integer> newLockedIds = lockToAllocate(mutex, lockedIds, orderIdsAvailableToLock, order.getQuantity());
        
        if (newLockedIds.isEmpty()) {
            throw new OrderNotHandledException("No matching orders");
        }

        for (Integer lockedId : newLockedIds) {
            Order existingOrder = ordersMap.get(lockedId);
            System.err.println("Executing order " + lockedId + " with " + existingOrder.getQuantity() + " for order: " + order.getId() + " " + order.getQuantity());
            executeOrders(existingOrder, order);
            releaseLock(mutex, lockedId, lockedIds);
        }
    }

    private void releaseLock(Object mutex, Integer lockedId, Set<Integer> lockedIds) {
        synchronized(mutex) {
            lockedIds.remove(lockedId);
        }
    }

    // separated to avoid overloading on synchronized mutex, need to use List as ordered structure
    public List<Integer> getOrderIdsAvailableToLock(Order order, List<Order> existingOrders, OrderComparisonFunction comparison) throws org.finances.OrderNotHandledException {
        int sum = 0;
        List<Integer> ordersrIdsToLock = new ArrayList<>();
        for (Order existingOrder : existingOrders) {
            if (comparison.run(order, existingOrder)) {
                ordersrIdsToLock.add(existingOrder.getId());
                sum += existingOrder.getQuantity();
            } else {
                break;
            }
        }
        if (sum == 0) {
            throw new OrderNotHandledException("No suiatable orders.");
        }
        return ordersrIdsToLock;
    }
    
    protected List<Integer> lockToAllocate(Object mutex, Set<Integer> lockIds, 
                                        List<Integer> orderIdsAvailableToLock, int allocation) throws OrderNotHandledException {
        int allocatedSum = 0;
        List<Integer> lockedIds = new ArrayList<>();
        synchronized(mutex) {
            for (Integer orderId : orderIdsAvailableToLock) {
                Order order = ordersMap.get(orderId);
                if (order == null) {
                    continue;
                }

                int orderQ = order.getQuantity();
                if (!lockIds.contains(orderId)) {
                    lockedIds.add(orderId);
                    lockIds.add(orderId);
                    allocatedSum += orderQ;
                    if (allocatedSum >= allocation) {
                        break;
                    }
                }
            }
        }
        if (allocatedSum == 0) {
            throw new OrderNotHandledException("There are no existing orders for ids: " + orderIdsAvailableToLock.toString());
        }
        return lockedIds;
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
            String buyOutput = formatBuyOrder(this.ordersToBuy, i);
            String sellOutput = formatSellOrder(this.ordersToSell, i);
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

    private String formatBuyOrder(List<Order> orders, int i) {
        String output = EMPTY_BUY_STRING;
        Order order = null;
        while (orders.size() > i) {
            // formatted values output
            order = orders.get(i);
            // delete order from list if it's null in map
            if (ordersMap.get(order.getId()) == null) {
                orders.remove(i);
                i += 1;
            } else {
                break;
            }
        }
        if (order == null) {
            output = String.format("|%10d|%13s|%7s", order.getId(), order.getFormattedVolume(), order.getFormattedPrice());
        }
        return output; 
    }

    private String formatSellOrder(List<Order> orders, int i) {
        String output = EMPTY_SELL_STRING;
        Order order = null;
        while (orders.size() > i) {
            // formatted values output
            order = orders.get(i);
            // delete order from list if it's null in map
            if (ordersMap.get(order.getId()) == null) {
                orders.remove(i);
                i += 1;
            } else {
                break;
            }
        }
        if (order != null) {
            output = String.format("|%7s|%13s|%10d|", order.getFormattedPrice(), order.getFormattedVolume(), order.getId());
        }
        return output; 
    }
}

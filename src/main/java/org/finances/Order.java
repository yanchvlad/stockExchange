

import java.text.NumberFormat;
import java.util.Locale;

// Field Index Type Description
// 0 char ‘B’ for a buy order, ‘S’ for a sell order
// 1 int unique order identifier (> 0)
// 2 short price in pence (> 0)
// 3 int quantity of order (> 0)
// 4 int peak size (> 0)
// Example:
// S,100345,5103,100000,10000
public class Order {
    private OrderType type;
    private Integer id;
    private Short price;
    private Integer quantity;
    private Integer peak = null;

    private static int STRING_VALUES_MAX_SIZE = 5;
    private static int STRING_VALUES_MIN_SIZE = 4;

    public boolean isBuy(){
        return this.type == OrderType.BUY;
    }

    public boolean isExecuted(){
        return quantity <= 0;
    }

    public Short getPrice() {
        return price;
    }

    private String formatNumber(int value) {
        return NumberFormat.getNumberInstance(Locale.US).format(value);
    }

    public String getFormattedPrice() {
        return formatNumber(getPrice());
    }

    public String getFormattedOrder() {
        if (isBuy()) {
            return String.format("|%10d|%13s|%7s", getId(), getFormattedVolume(), getFormattedPrice());
        }
        return String.format("|%7s|%13s|%10d|", getFormattedPrice(), getFormattedVolume(), getId());
    }

    public String getFormattedVolume() {
        int volume = getQuantity();
        if (isIceberg()) {
            volume = Math.min(getQuantity(), getPeak());
        }
        return formatNumber(volume);
    }

    public Integer getPeak() {
        return peak;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public OrderType getType() {
        return type;
    }

    public Integer getId() {
        return id;
    }

    public boolean isIceberg(){
        return this.peak != null;
    }

    // validation on arguments, should be extended by every type of it
    public static boolean isValid(String ordeString){
        if (ordeString == null || ordeString.isEmpty()) {
            System.err.println("Empty ordeString");
            return false;
        }
        String[] values = ordeString.split(",");
        if  ( STRING_VALUES_MIN_SIZE <= values.length && values.length <= STRING_VALUES_MAX_SIZE) {
            for (int i = 1; i < values.length; i++) {
                try {
                    if (Integer.parseInt(values[i]) <= 0) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public Order(String ordeString) throws OrderException {
        String[] values = ordeString.split(",");
        this.type = OrderType.valueOfLabel(values[0]);
        this.id = Integer.parseInt(values[1]);
        this.price = Short.parseShort(values[2]);
        this.quantity = Integer.parseInt(values[3]);
        if (values.length == STRING_VALUES_MAX_SIZE) {
            int peak = Integer.parseInt(values[4]);
            if (peak > 0) {
                this.peak = Integer.parseInt(values[4]);
            } else {
                throw new OrderException("Incorrect value of peak, should be more than 0.");
            }
        }
    }

    public int execute(Integer allocationQuantity) {
        int diff = Math.min(getQuantity(), allocationQuantity);
        this.quantity -= diff;
        return diff;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s$,%s/%s", type, id, price, quantity, peak);
    }

}

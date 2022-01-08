

public enum OrderType {
    SELL("S"), BUY("B");

    public final String label;

    private OrderType(String label) {
        this.label = label;
    }

    public static OrderType valueOfLabel(String label) {
        for (OrderType e : values()) {
            if (e.label.equals(label)) {
                return e;
            }
        }
        return null;
    }
}

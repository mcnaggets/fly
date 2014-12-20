package by.fly.model.facet;

public class ValueCount {
    private String value;
    private int count;

    public ValueCount(String value, int count) {
        this.value = value;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return count + " - " + value;
    }
}
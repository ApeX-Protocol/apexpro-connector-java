package exchange.apexpro.connector.model.enums;

public enum EventFrequency {

    H("H"), //H means High frequency,
    M("M"); //M means middle frequency

    private final String code;

    EventFrequency(String side) {
        this.code = side;
    }

    @Override
    public String toString() {
        return code;
    }

}
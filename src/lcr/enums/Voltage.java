package lcr.enums;

public enum Voltage {
    MV0(0),
    MV100(100),
    MV300(300),
    MV600(600),
    V1(1000);

    private final int value; // Volvemos a su nombre original

    Voltage(int value) {
        this.value = value;
    }

    public int getValue() {
        return value; // Devolverá 100, 300, 600, 1000
    }

    public static Voltage fromValue(int val) {
        for (Voltage v : Voltage.values()) {
            if (v.getValue() == val) {
                return v;
            }
        }
        throw new IllegalArgumentException("Voltaje no soportado por el hardware: " + val + " mV");
    }

    @Override
    public String toString() {
        if (value >= 1000) {
            return (value / 1000) + "V";
        }
        return value + "mV";
    }
}
package lcr.enums;

/**
 * Representation of the instrument's operational AC test signal frequencies,
 * including target hardware adjustments for specific instrument edge-cases.
 * * @author sylkat
 */
public enum Frequency {
    HZ0(0),
    HZ100(100),
    HZ120(120),
    HZ1000(1000),
    HZ9999(9999),
    HZ40000(40000),
    HZ100000(100000);

    private final int value;

    /**
     * Constructs the frequency enumeration entry with its literal hertz value representation.
     * * @param value the AC test signal frequency magnitude in Hertz (Hz)
     */
    Frequency(int value) {
        this.value = value;
    }

    /**
     * Gets the concrete numeric frequency magnitude value in Hertz.
     * * @return the integer hertz value
     */
    public int getValue() {
        return value;
    }

    /**
     * Resolves a raw numeric hertz value into its matching Frequency enumeration token,
     * accounting for hardware-specific frequency adjustment limits (e.g., matching 10000 Hz to 9999 Hz).
     * * @param value the incoming raw integer frequency value to evaluate
     * @return the resolved Frequency matching enum instance
     * @throws IllegalArgumentException if the provided value cannot be matched to any supported hardware tier
     */
    public static Frequency fromValue(int value) {
        if (value == 10000) {
            value = 9999;
        }

        for (Frequency f : Frequency.values()) {
            if (f.getValue() == value) {
                return f;
            }
        }
        throw new IllegalArgumentException("Frequency tier not supported by hardware: " + value + " Hz");
    }

    @Override
    public String toString() {
        if (this == HZ9999) return "10 kHz";
        if (value >= 1000) return (value / 1000) + " kHz";
        return value + " Hz";
    }
}
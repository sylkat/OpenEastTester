package lcr.enums;

/**
 * Representation of the internal physical DC bias voltage injection steps,
 * used during component testing under specific operating voltage offsets.
 * * @author sylkat
 */
public enum BiasVoltage {

    OFF(0),
    MV100(100),
    MV200(200),
    MV300(300),
    MV500(500);

    private final int value;

    /**
     * Constructs the bias voltage step with its magnitude in millivolts.
     * * @param value the target voltage parameter in millivolts (mV)
     */
    BiasVoltage(int value) {
        this.value = value;
    }

    /**
     * Gets the numeric electrical magnitude representation in millivolts.
     * * @return the integer millivolt level
     */
    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (this == OFF) {
            return "OFF";
        }
        return value + " mV";
    }

    /**
     * Resolves an incoming string label descriptor or raw millivolt value
     * key token into its matching BiasVoltage enumeration variant.
     * * @param text the target string payload to evaluate
     * @return the matched BiasVoltage instance token, or null if the input is null
     * @throws IllegalArgumentException if the provided descriptor token cannot be resolved
     */
    public static BiasVoltage fromString(String text) {
        if (text == null) {
            return null;
        }

        String cleanText = text.trim();

        for (BiasVoltage a : BiasVoltage.values()) {
            if (a.name().equalsIgnoreCase(cleanText) ||
                    String.valueOf(a.getValue()).equals(cleanText) ||
                    a.toString().equalsIgnoreCase(cleanText)) {
                return a;
            }
        }
        throw new IllegalArgumentException("BiasVoltage not supported: " + text);
    }
}
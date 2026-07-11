package lcr.enums;

/**
 * Representation of the discrete manual hardware resistance range brackets
 * utilized by the instrument's internal measurement bridge subsystems.
 * * @author sylkat
 */
public enum Range {
    R100(100),
    R1000(1000),
    R10000(10000),
    R100000(100000);

    private final int value;

    /**
     * Constructs the electrical hardware range step with its base ohm multiplier.
     * * @param value the nominal electrical upper limit in Ohms (Ω)
     */
    Range(int value) {
        this.value = value;
    }

    /**
     * Gets the base numeric ohm multiplier value for this specific scale bracket.
     * * @return the integer Ohm level value
     */
    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (value >= 1000) {
            int kValue = value / 1000;
            return kValue + " kΩ";
        }
        return value + " Ω";
    }

    /**
     * Resolves an incoming configuration label descriptor, name string, or raw
     * numeric multiplier token into its structural Range enumeration equivalent.
     * * @param text the target string payload to evaluate
     * @return the resolved Range enumeration token, or null if the input is null
     * @throws IllegalArgumentException if the provided descriptor token cannot be matched
     */
    public static Range fromString(String text) {
        if (text == null) {
            return null;
        }

        String cleanText = text.trim();

        for (Range r : Range.values()) {
            if (String.valueOf(r.getValue()).equals(cleanText) ||
                    r.name().equalsIgnoreCase(cleanText) ||
                    r.toString().equalsIgnoreCase(cleanText)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Range bracket not supported: " + text);
    }
}
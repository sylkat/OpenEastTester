package lcr.enums;

/**
 * Representation of the instrument's operational AC test signal voltage levels,
 * tracking magnitudes in millivolts for precision hardware configuration mapping.
 * * @author sylkat
 */
public enum Voltage {
    MV0(0),
    MV100(100),
    MV300(300),
    MV600(600),
    V1(1000);

    private final int value;

    /**
     * Constructs the voltage tier with its base magnitude in millivolts.
     * * @param value the nominal AC signal level in millivolts (mV)
     */
    Voltage(int value) {
        this.value = value;
    }

    /**
     * Gets the base numeric signal level magnitude value in millivolts.
     * * @return the integer millivolt value
     */
    public int getValue() {
        return value;
    }

    /**
     * Resolves a raw numeric millivolt value key token into its matching
     * Voltage structural enumeration tier.
     * * @param val the incoming raw integer millivolt level to evaluate
     * @return the resolved Voltage matching enum instance
     * @throws IllegalArgumentException if the provided value cannot be matched to a supported hardware step
     */
    public static Voltage fromValue(int val) {
        for (Voltage v : Voltage.values()) {
            if (v.getValue() == val) {
                return v;
            }
        }
        throw new IllegalArgumentException("Voltage level not supported by hardware: " + val + " mV");
    }

    @Override
    public String toString() {
        if (value >= 1000) {
            return (value / 1000) + "V";
        }
        return value + "mV";
    }
}
package lcr.beans;

/**
 * Immutable object representing a single pair of readings from the LCR meter.
 * * @author sylkat
 */
public class Measurement {

    private final double primaryValue;
    private final double secondaryValue;

    /**
     * Constructs a measurement point with primary and secondary values.
     * * @param primaryValue   the main reading value (e.g., Capacitance, Inductance)
     * @param secondaryValue the dissipation, quality, or resistance parameter (e.g., D, Q, ESR)
     */
    public Measurement(double primaryValue, double secondaryValue) {
        this.primaryValue = primaryValue;
        this.secondaryValue = secondaryValue;
    }

    public double getPrimaryValue() { return primaryValue; }
    public double getSecondaryValue() { return secondaryValue; }
}
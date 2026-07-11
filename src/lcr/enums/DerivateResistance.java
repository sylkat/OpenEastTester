package lcr.enums;

/**
 * Representation of secondary or derived complex electrical parameters computed
 * alongside primary Resistance (R) measurements under AC test signals.
 * * @author sylkat
 */
public enum DerivateResistance {

    IMPEDANCE("Z", "Impedance"),
    PHASE_ANGLE("THR", "Phase Angle"),
    QUALITY_FACTOR("Q", "Quality Factor"),
    LOSS_FACTOR("D", "Loss Factor"),
    PARASITIC_INDUCTANCE("L", "Parasitic Inductance"),
    PARASITIC_CAPACITANCE("C", "Parasitic Capacitance");

    private final String symbol;
    private final String label;

    /**
     * Constructs the derivative parameter entry with its standard acronym symbol and readable label descriptor.
     * * @param symbol the short acronym metric identifier (e.g., "Z")
     * @param label  the full verbose mathematical or physical name string
     */
    DerivateResistance(String symbol, String label) {
        this.symbol = symbol;
        this.label = label;
    }

    /**
     * Gets the abbreviated electrical metric acronym or symbol text.
     * * @return the parameter symbol string
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Gets the full human-readable engineering label text for UI display bindings.
     * * @return the descriptive parameter name string
     */
    public String getLabel() {
        return label;
    }
}
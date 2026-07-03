package et431.enums;

public enum DerivateCapacitance {
    EQUIVALENT_SERIES_RESISTANCE("ESR", "Equivalent Series Resistance"),
    REACTANCE("X", "Reactance"),
    IMPEDANCE("Z", "Impedance"),
    PHASE_ANGLE("THR", "Phase Angle"),
    QUALITY_FACTOR("Q", "Quality Factor");

    private final String symbol;
    private final String label;

    DerivateCapacitance(String symbol, String label) {
        this.symbol = symbol;
        this.label = label;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getLabel() {
        return label;
    }
}
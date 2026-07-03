package et431.enums;

public enum DerivateInductance {
    SERIES_RESISTANCE("ESR", "Equivalent Series Resistance"),
    REACTANCE("X", "Reactance"),
    IMPEDANCE("Z", "Impedance"),
    PHASE_ANGLE("THR", "Phase Angle"),
    LOSS_FACTOR("D", "Loss Factor");

    private final String symbol;
    private final String label;

    DerivateInductance(String symbol, String label) {
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

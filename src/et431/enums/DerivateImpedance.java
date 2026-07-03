package et431.enums;

public enum DerivateImpedance {
    RESISTANCE("R", "Resistance (ESR)"),
    PHASE_ANGLE("THR", "Phase Angle"),
    QUALITY_FACTOR("Q", "Quality Factor"),
    LOSS_FACTOR("D", "Loss Factor"),
    PARASITIC_INDUCTANCE("L", "Parasitic Inductance"),
    PARASITIC_CAPACITANCE("C", "Parasitic Capacitance");

    private final String symbol;
    private final String label;

    DerivateImpedance(String symbol, String label) {
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

package et431.util;

public final class Constants {



    // Ocultar constructor para evitar instanciación de clase de utilidades
    private Constants() {}
    public static final int DEBUG = 1;
    // --- LCR DISPLAY LABELS ---
    public static final String LABEL_RESISTANCE    = "(R) Resistance";
    public static final String LABEL_CAPACITANCE   = "(C) Capacitance";
    public static final String LABEL_INDUCTANCE    = "(L) Inductance";
    public static final String LABEL_IMPEDANCE     = "(Z) Impedance";
    public static final String LABEL_REACTANCE     = "(X) Reactance";
    public static final String LABEL_LOSS_FACTOR   = "(D) Loss Factor";
    public static final String LABEL_QUALITY_FACTOR = "(Q) Quality Factor";
    public static final String LABEL_PHASE_ANGLE   = "(θ) Phase Angle";
    public static final String LABEL_ESR           = "(ESR)";

    // --- DERIVED METRIC LABELS FOR DISPLAY GRID ---

    public static final String[] labelsResistance = {
            "Impedance (Z)", "Phase Angle (θ)",
            "Quality (Q)",   "Loss Factor (D)",
            "Parasitic L",   "Parasitic C"
    };

    public static final String[] labelsCapacitance = {
            "ESR (Ω)",       "Reactance (X)",
            "Impedance (Z)", "Phase Angle (θ)",
            "Quality (Q)",   ""
    };

    public static final String[] labelsInductance = {
            "ESR (Ω)",       "Reactance (X)",
            "Impedance (Z)", "Phase Angle (θ)",
            "Loss Factor (D)",""
    };

    public static final String[] labelsImpedance = {
            "Resistance (R)","Phase Angle (θ)",
            "Quality (Q)",   "Loss Factor (D)",
            "Parasitic L",   "Parasitic C"
    };
}
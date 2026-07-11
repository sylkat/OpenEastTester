package lcr.util;

/**
 * System-wide constants container managing user interface text literals,
 * execution log flags, and structural measurement display matrix definitions.
 * * @author sylkat
 */
public final class Constants {

    /**
     * The official application title header identity string.
     */
    public static final String TITLE_APP = "Open LCR";

    /**
     * Hardware debugging execution level flag identifier.
     */
    public static final int DEBUG = 1;

    /**
     * System flag controlling runtime console logging stream operations.
     */
    public static boolean SHOW_LOGS = true;

    // --- LCR DISPLAY LABELS ---
    public static final String LABEL_RESISTANCE     = "(R) Resistance";
    public static final String LABEL_CAPACITANCE    = "(C) Capacitance";
    public static final String LABEL_ECAP           = "ECAP";
    public static final String LABEL_INDUCTANCE     = "(L) Inductance";
    public static final String LABEL_IMPEDANCE      = "(Z) Impedance";
    public static final String LABEL_REACTANCE      = "(X) Reactance";
    public static final String LABEL_LOSS_FACTOR    = "(D) Loss Factor";
    public static final String LABEL_QUALITY_FACTOR = "(Q) Quality Factor";
    public static final String LABEL_PHASE_ANGLE    = "(θ) Phase Angle";
    public static final String LABEL_ESR            = "(ESR)";

    // --- DERIVED METRIC LABELS FOR DISPLAY GRID ---

    /**
     * Presentation grid layouts for automatic standard operating modes.
     */
    public static final String[] LABELS_AUTO = {
            "", "",
            "", "",
            "", ""
    };

    /**
     * Secondary derived parameters mapping grid used alongside primary Resistance measurements.
     */
    public static final String[] LABELS_RESISTANCE = {
            "Impedance (Z)", "Phase Angle (θ)",
            "Quality (Q)",   "Loss Factor (D)",
            "Parasitic L",   "Parasitic C"
    };

    /**
     * Secondary derived parameters mapping grid used alongside primary Capacitance measurements.
     */
    public static final String[] LABELS_CAPACITANCE = {
            "ESR (Ω)",       "Reactance (X)",
            "Impedance (Z)", "Phase Angle (θ)",
            "Quality (Q)",   ""
    };

    /**
     * Secondary derived parameters mapping grid used alongside primary Inductance measurements.
     */
    public static final String[] LABELS_INDUCTANCE = {
            "ESR (Ω)",       "Reactance (X)",
            "Impedance (Z)", "Phase Angle (θ)",
            "Loss Factor (D)", ""
    };

    /**
     * Secondary derived parameters mapping grid used alongside primary Impedance measurements.
     */
    public static final String[] LABELS_IMPEDANCE = {
            "Resistance (R)", "Phase Angle (θ)",
            "Quality (Q)",    "Loss Factor (D)",
            "Parasitic L",    "Parasitic C"
    };

    /**
     * Private constructor to enforce static utility access control patterns
     * and strictly block external class object instantiation.
     */
    private Constants() {}
}
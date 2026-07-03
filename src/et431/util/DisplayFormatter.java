package et431.util;

import static et431.util.Constants.*;

public class DisplayFormatter {

    /**
     * Maps the short LCR meter display labels to their full technical names,
     * prefixing the original physical device token for rapid visual cross-referencing.
     */
    public static String getDisplayLabel(String displayLabel) {
        if (displayLabel == null) return "";

        String token = displayLabel.trim().toUpperCase();

        switch (token) {
            case "R":
                return LABEL_RESISTANCE;
            case "C":
                return LABEL_CAPACITANCE;
            case "L":
                return LABEL_INDUCTANCE;
            case "Z":
                return LABEL_IMPEDANCE;
            case "X":
                return LABEL_REACTANCE;
            case "D":
                return LABEL_LOSS_FACTOR;
            case "Q":
                return LABEL_QUALITY_FACTOR;
            case "THR":
                return LABEL_PHASE_ANGLE;
            case "ESR":
                return LABEL_ESR;
            default:
                return displayLabel;
        }
    }
}
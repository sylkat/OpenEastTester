package lcr.util;

import static lcr.util.Constants.*;

/**
 * Utility utility class responsible for formatting component parameter labels,
 * converting raw instrument acronym tokens into verbose engineering strings for UI bindings.
 * * @author sylkat
 */
public class DisplayFormatter {

    /**
     * Maps the short LCR meter display labels to their full technical names,
     * using constants definitions for system-wide presentation uniformity.
     * * @param displayLabel the short raw token symbol received from the meter device
     * @return the expanded technical label identifier string, or the original token if unmatched
     */
    public static String getDisplayLabel(String displayLabel) {
        if (displayLabel == null) {
            return "";
        }

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
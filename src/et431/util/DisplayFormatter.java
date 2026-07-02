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

        return switch (token) {
            case "R"   -> LABEL_RESISTANCE;
            case "C"   -> LABEL_CAPACITANCE;
            case "L"   -> LABEL_INDUCTANCE;
            case "Z"   -> LABEL_IMPEDANCE;
            case "X"   -> LABEL_REACTANCE;
            case "D"   -> LABEL_LOSS_FACTOR;
            case "Q"   -> LABEL_QUALITY_FACTOR;
            case "THR" -> LABEL_PHASE_ANGLE;
            case "ESR" -> LABEL_ESR;
            default    -> displayLabel;
        };
    }
}
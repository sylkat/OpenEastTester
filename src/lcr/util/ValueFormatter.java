package lcr.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility formatter engineered to convert raw scalar meter components into sanitized,
 * scannable engineering and standard SI notation strings using custom precision scaling rules.
 * * @author sylkat
 */
public final class ValueFormatter {

    private static final DecimalFormat FORMAT;

    static {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        FORMAT = new DecimalFormat("0.####", symbols);
        FORMAT.setGroupingUsed(false);
    }

    /**
     * Private constructor to enforce static utility access control patterns
     * and strictly block external class object instantiation.
     */
    private ValueFormatter() {}

    /**
     * Converts raw impedance values into standard scaled metric variants using kilo-ohm limits.
     * * @param rawValue the primitive scalar double value under translation
     * @return a sanitized engineering formatted impedance literal string
     */
    public static String formatImpedance(double rawValue) {
        double ohms = rawValue / 1000.0;
        return formatSI(ohms, "Ω");
    }

    /**
     * Dispatches raw engineering scaling configurations across targeted prefix structural
     * limits, injecting normalized micro, nano, pico or mega constraints while dropping
     * fractional notations for clean Ohm boundaries.
     * * @param value the raw structural double magnitude value to scale
     * @param unit  the destination engineering unit literal identifier token (e.g., "F", "H", "Ω")
     * @return the completely scaled engineering presentation token layout string
     */
    public static String formatSI(double value, String unit) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return String.valueOf(value);
        }
        if (value == 0.0) {
            return unit.isEmpty() ? "0" : "0 " + unit;
        }

        double abs = Math.abs(value);
        String prefix = "";

        // Scale up for large values
        if (abs >= 1e9) {
            value /= 1e9;
            prefix = "G";
        } else if (abs >= 1e6) {
            value /= 1e6;
            prefix = "M";
        } else if (abs >= 1e3) {
            value /= 1e3;
            prefix = "k";
        }
        // Scale down for small values, skipping fractional prefixes for Ohms
        else if (abs < 1.0 && !"Ω".equals(unit)) {
            if (abs >= 1e-3) {
                value /= 1e-3;
                prefix = "m";
            } else if (abs >= 1e-6) {
                value /= 1e-6;
                prefix = "µ";
            } else if (abs >= 1e-9) {
                value /= 1e-9;
                prefix = "n";
            } else if (abs >= 1e-12) {
                value /= 1e-12;
                prefix = "p";
            } else if (abs >= 1e-15) {
                value /= 1e-12;
                prefix = "p";
            } else {
                value = abs;
            }
        }

        String result = FORMAT.format(value);
        if (unit == null || unit.isEmpty()) {
            return prefix.isEmpty() ? result : result + prefix;
        }
        return prefix.isEmpty() ? result + unit : result + prefix + unit;
    }
}
package et431.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Utility class to format raw meter values into readable engineering/SI notation.
 */
public final class ValueFormatter {

    private static final DecimalFormat FORMAT;

    static {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        FORMAT = new DecimalFormat("0.####", symbols);
        FORMAT.setGroupingUsed(false);
    }

    private ValueFormatter() {
    }

    public static String formatImpedance(double rawValue) {
        double ohms = rawValue / 1000.0;
        return formatSI(ohms, "Ω");
    }

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
        else if (abs < 1.0 && !unit.equals("Ω")) {
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
            }
        }
        else {
            prefix = "";
        }
        String result = FORMAT.format(value);
        if (unit == null || unit.isEmpty()) {
            return prefix.isEmpty() ? result : result + "" + prefix;
        }
        return prefix.isEmpty() ? result + "" + unit : result + "" + prefix + unit;
    }
}
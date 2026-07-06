package lcr.util;

import lcr.enums.DerivateCapacitance;

import java.util.EnumMap;
import java.util.Map;

public class CapacitanceDerivator {

    /**
     * Calculates all derived parameters based on C, D, and the test frequency.
     *
     * @param c In farads (Capacitance)
     * @param d Dissipation/Loss factor (dimensionless)
     * @param frequency In Hz (e.g., 1000 for 1kHz).
     * @return An EnumMap containing all the calculated derived parameters.
     */
    public static Map<DerivateCapacitance, Double> calculate(double c, double d, double frequency) {
        Map<DerivateCapacitance, Double> results = new EnumMap<>(DerivateCapacitance.class);

        // Avoid invalid or zero frequency/capacitance to prevent division by zero
        if (frequency > 0 && c != 0) {
            double angularFrequency = 2 * Math.PI * frequency;

            // 1. Capacitive Reactance (X) = -1 / (2 * pi * f * C)
            // It is negative because it's capacitive
            double reactance = -1.0 / (angularFrequency * c);
            results.put(DerivateCapacitance.REACTANCE, reactance);

            // 2. Equivalent Series Resistance (ESR) = D / (2 * pi * f * C)
            double esr = d / (angularFrequency * c);
            results.put(DerivateCapacitance.EQUIVALENT_SERIES_RESISTANCE, esr);

            // 3. Total Impedance (Z) = sqrt(ESR² + X²)
            double impedance = Math.sqrt(Math.pow(esr, 2) + Math.pow(reactance, 2));
            results.put(DerivateCapacitance.IMPEDANCE, impedance);

            // 4. Phase Angle (THR) in degrees = atan2(X, ESR)
            double phaseAngle = Math.toDegrees(Math.atan2(reactance, esr));
            results.put(DerivateCapacitance.PHASE_ANGLE, phaseAngle);

            // 5. Quality Factor (Q) = 1 / D (Avoid division by zero if D is 0)
            double qualityFactor = (d != 0) ? 1.0 / d : Double.POSITIVE_INFINITY;
            results.put(DerivateCapacitance.QUALITY_FACTOR, qualityFactor);

        } else {
            // Default safe values if input parameters are zero or invalid
            results.put(DerivateCapacitance.REACTANCE, 0.0);
            results.put(DerivateCapacitance.EQUIVALENT_SERIES_RESISTANCE, 0.0);
            results.put(DerivateCapacitance.IMPEDANCE, 0.0);
            results.put(DerivateCapacitance.PHASE_ANGLE, -90.0); // Pure capacitor phase
            results.put(DerivateCapacitance.QUALITY_FACTOR, 0.0);
        }

        return results;
    }
}
package et431.util;

import et431.enums.DerivateResistance;

import java.util.EnumMap;
import java.util.Map;

public class ResistanceDerivator {

    /**
     * Calculates all derived parameters based on R, X, and the test frequency.
     *
     * @param r In ohms (Resistance)
     * @param x In ohms (Reactance). Positive for inductive, negative for capacitive.
     * @param frequency In Hz (e.g., 1000 for 1kHz).
     * @return An EnumMap containing all the calculated derived parameters.
     */
    public static Map<DerivateResistance, Double> calculate(double r, double x, double frequency) {
        Map<DerivateResistance, Double> results = new EnumMap<>(DerivateResistance.class);

        // 1. Total Impedance (Z) = sqrt(R² + X²)
        double impedance = Math.sqrt(Math.pow(r, 2) + Math.pow(x, 2));
        results.put(DerivateResistance.IMPEDANCE, impedance);

        // 2. Phase Angle (THR) in degrees = atan2(X, R)
        double phaseAngle = Math.toDegrees(Math.atan2(x, r));
        results.put(DerivateResistance.PHASE_ANGLE, phaseAngle);

        // 3. Quality Factor (Q) = |X| / R (Avoid division by zero)
        double qualityFactor = (r != 0) ? Math.abs(x) / r : Double.POSITIVE_INFINITY;
        results.put(DerivateResistance.QUALITY_FACTOR, qualityFactor);

        // 4. Loss Factor / Dissipation (D) = R / |X| (Avoid division by zero)
        double lossFactor = (x != 0) ? r / Math.abs(x) : Double.POSITIVE_INFINITY;
        results.put(DerivateResistance.LOSS_FACTOR, lossFactor);

        // Parasitic calculations based on frequency (Avoid invalid or zero frequencies)
        if (frequency > 0 && x != 0) {
            double angularFrequency = 2 * Math.PI * frequency;

            if (x > 0) {
                // 5. Parasitic Inductance (L) = X / (2 * pi * f)
                double L = x / angularFrequency;
                results.put(DerivateResistance.PARASITIC_INDUCTANCE, L);
                results.put(DerivateResistance.PARASITIC_CAPACITANCE, 0.0); // Not applicable
            } else {
                // 6. Parasitic Capacitance (C) = 1 / (2 * pi * f * |X|)
                double C = 1.0 / (angularFrequency * Math.abs(x));
                results.put(DerivateResistance.PARASITIC_CAPACITANCE, C);
                results.put(DerivateResistance.PARASITIC_INDUCTANCE, 0.0); // Not applicable
            }
        } else {
            results.put(DerivateResistance.PARASITIC_INDUCTANCE, 0.0);
            results.put(DerivateResistance.PARASITIC_CAPACITANCE, 0.0);
        }

        return results;
    }
}

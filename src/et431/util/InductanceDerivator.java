package et431.util;

import et431.enums.DerivateInductance;

import java.util.EnumMap;
import java.util.Map;

public class InductanceDerivator {

    /**
     * Calculates all derived parameters based on L, Q, and the test frequency.
     *
     * @param l In henries (Inductance)
     * @param q Quality factor (dimensionless)
     * @param frequency In Hz (e.g., 1000 for 1kHz).
     * @return An EnumMap containing all the calculated derived parameters.
     */
    public static Map<DerivateInductance, Double> calculate(double l, double q, double frequency) {
        Map<DerivateInductance, Double> results = new EnumMap<>(DerivateInductance.class);

        // Avoid invalid or zero frequency/inductance to prevent errors
        if (frequency > 0 && l > 0) {
            double angularFrequency = 2 * Math.PI * frequency;

            // 1. Inductive Reactance (X) = 2 * pi * f * L
            // It is positive because it's inductive
            double reactance = angularFrequency * l;
            results.put(DerivateInductance.REACTANCE, reactance);

            // 2. Equivalent Series Resistance (ESR) = X / Q
            // Avoid division by zero if Q is perfectly 0
            double esr = (q != 0) ? reactance / q : Double.POSITIVE_INFINITY;
            results.put(DerivateInductance.SERIES_RESISTANCE, esr);

            // 3. Total Impedance (Z) = sqrt(ESR² + X²)
            // If ESR is infinity (Q=0), Impedance is also handled safely
            double impedance = Double.isInfinite(esr) ? Double.POSITIVE_INFINITY : Math.sqrt(Math.pow(esr, 2) + Math.pow(reactance, 2));
            results.put(DerivateInductance.IMPEDANCE, impedance);

            // 4. Phase Angle (THR) in degrees = atan2(X, ESR)
            // If ESR is 0 (Q is infinite), phase angle will be exactly 90 degrees
            double phaseAngle = Math.toDegrees(Math.atan2(reactance, esr));
            results.put(DerivateInductance.PHASE_ANGLE, phaseAngle);

            // 5. Loss Factor / Dissipation (D) = 1 / Q
            double lossFactor = (q != 0) ? 1.0 / q : Double.POSITIVE_INFINITY;
            results.put(DerivateInductance.LOSS_FACTOR, lossFactor);

        } else {
            // Default safe values if input parameters are zero or invalid
            results.put(DerivateInductance.REACTANCE, 0.0);
            results.put(DerivateInductance.SERIES_RESISTANCE, 0.0);
            results.put(DerivateInductance.IMPEDANCE, 0.0);
            results.put(DerivateInductance.PHASE_ANGLE, 90.0); // Pure inductor phase
            results.put(DerivateInductance.LOSS_FACTOR, 0.0);
        }

        return results;
    }
}

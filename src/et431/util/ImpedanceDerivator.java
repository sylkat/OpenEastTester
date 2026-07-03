package et431.util;

import et431.enums.DerivateImpedance;

import java.util.EnumMap;
import java.util.Map;

public class ImpedanceDerivator {

    /**
     * Calculates all derived parameters based on Z, X, and the test frequency.
     *
     * @param z In ohms (Total Impedance Magnitude)
     * @param x In ohms (Reactance). Positive for inductive, negative for capacitive.
     * @param frequency In Hz (e.g., 1000 for 1kHz).
     * @return An EnumMap containing all the calculated derived parameters.
     */
    public static Map<DerivateImpedance, Double> calculate(double z, double x, double frequency) {
        Map<DerivateImpedance, Double> results = new EnumMap<>(DerivateImpedance.class);

        // 1. Calculate Real Resistance (R) = sqrt(Z² - X²)
        // Math.max handles floating-point inaccuracies where x² might slightly exceed z²
        double rSquared = Math.max(0.0, Math.pow(z, 2) - Math.pow(x, 2));
        double r = Math.sqrt(rSquared);
        results.put(DerivateImpedance.RESISTANCE, r);

        // 2. Phase Angle (THR) in degrees = atan2(X, R)
        double phaseAngle = Math.toDegrees(Math.atan2(x, r));
        results.put(DerivateImpedance.PHASE_ANGLE, phaseAngle);

        // 3. Quality Factor (Q) = |X| / R (Avoid division by zero)
        double qualityFactor = (r != 0) ? Math.abs(x) / r : Double.POSITIVE_INFINITY;
        results.put(DerivateImpedance.QUALITY_FACTOR, qualityFactor);

        // 4. Loss Factor / Dissipation (D) = R / |X| (Avoid division by zero)
        double lossFactor = (x != 0) ? r / Math.abs(x) : Double.POSITIVE_INFINITY;
        results.put(DerivateImpedance.LOSS_FACTOR, lossFactor);

        // Parasitic calculations based on frequency and the sign of Reactance (X)
        if (frequency > 0 && x != 0) {
            double angularFrequency = 2 * Math.PI * frequency;

            if (x > 0) {
                // 5. Parasitic Inductance (L) = X / (2 * pi * f)
                double L = x / angularFrequency;
                results.put(DerivateImpedance.PARASITIC_INDUCTANCE, L);
                results.put(DerivateImpedance.PARASITIC_CAPACITANCE, 0.0);
            } else {
                // 6. Parasitic Capacitance (C) = 1 / (2 * pi * f * |X|)
                double C = 1.0 / (angularFrequency * Math.abs(x));
                results.put(DerivateImpedance.PARASITIC_CAPACITANCE, C);
                results.put(DerivateImpedance.PARASITIC_INDUCTANCE, 0.0);
            }
        } else {
            results.put(DerivateImpedance.PARASITIC_INDUCTANCE, 0.0);
            results.put(DerivateImpedance.PARASITIC_CAPACITANCE, 0.0);
        }

        return results;
    }
}

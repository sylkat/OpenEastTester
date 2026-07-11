package lcr.util;

import lcr.enums.DerivateImpedance;
import java.util.EnumMap;
import java.util.Map;

/**
 * Utility calculator processing secondary complex electrical parameters derived from
 * primary Impedance (Z) magnitude and Reactance (X) metrics under dynamic AC test frequencies.
 * * @author sylkat
 */
public class ImpedanceDerivator {

    /**
     * Calculates the complete structural mapping matrix of secondary derived parameter assets
     * based on total impedance magnitude, reactive state elements, and operational signal frequencies.
     * * @param z         the primary measured total impedance magnitude in Ohms (Ω)
     * @param x         the reactive parameter in Ohms (Ω), positive for inductive, negative for capacitive
     * @param frequency the nominal hardware AC test signal operational frequency value in Hertz (Hz)
     * @return a structured EnumMap containing all successfully resolved complex derivative measurements
     */
    public static Map<DerivateImpedance, Double> calculate(double z, double x, double frequency) {
        Map<DerivateImpedance, Double> results = new EnumMap<>(DerivateImpedance.class);

        // 1. Calculate Real Resistance (R) = sqrt(Z² - X²)
        double rSquared = Math.max(0.0, Math.pow(z, 2) - Math.pow(x, 2));
        double r = Math.sqrt(rSquared);
        results.put(DerivateImpedance.RESISTANCE, r);

        // 2. Phase Angle (THR) in degrees = atan2(X, R)
        double phaseAngle = Math.toDegrees(Math.atan2(x, r));
        results.put(DerivateImpedance.PHASE_ANGLE, phaseAngle);

        // 3. Quality Factor (Q) = |X| / R
        double qualityFactor = (r != 0) ? Math.abs(x) / r : Double.POSITIVE_INFINITY;
        results.put(DerivateImpedance.QUALITY_FACTOR, qualityFactor);

        // 4. Loss Factor / Dissipation (D) = R / |X|
        double lossFactor = (x != 0) ? r / Math.abs(x) : Double.POSITIVE_INFINITY;
        results.put(DerivateImpedance.LOSS_FACTOR, lossFactor);

        // Parasitic element analysis based on frequency bounds and reactive load polarities
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
            // Default safe boundaries for hardware initialization states or zero lines
            results.put(DerivateImpedance.PARASITIC_INDUCTANCE, 0.0);
            results.put(DerivateImpedance.PARASITIC_CAPACITANCE, 0.0);
        }

        return results;
    }
}
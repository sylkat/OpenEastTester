package lcr.util;

import lcr.enums.DerivateResistance;
import java.util.EnumMap;
import java.util.Map;

/**
 * Utility calculator processing secondary complex electrical parameters derived from
 * primary Resistance (R) and Reactance (X) metrics under dynamic AC test frequencies.
 * * @author sylkat
 */
public class ResistanceDerivator {

    /**
     * Calculates the complete structural mapping matrix of secondary derived parameter assets
     * based on primary resistance, reactive state elements, and operational signal frequencies.
     * * @param r         the primary measured resistance magnitude value in Ohms (Ω)
     * @param x         the reactive parameter in Ohms (Ω), positive for inductive, negative for capacitive
     * @param frequency the nominal hardware AC test signal operational frequency value in Hertz (Hz)
     * @return a structured EnumMap containing all successfully resolved complex derivative measurements
     */
    public static Map<DerivateResistance, Double> calculate(double r, double x, double frequency) {
        Map<DerivateResistance, Double> results = new EnumMap<>(DerivateResistance.class);

        // 1. Total Impedance (Z) = sqrt(R² + X²)
        double impedance = Math.sqrt(Math.pow(r, 2) + Math.pow(x, 2));
        results.put(DerivateResistance.IMPEDANCE, impedance);

        // 2. Phase Angle (THR) in degrees = atan2(X, R)
        double phaseAngle = Math.toDegrees(Math.atan2(x, r));
        results.put(DerivateResistance.PHASE_ANGLE, phaseAngle);

        // 3. Quality Factor (Q) = |X| / R
        double qualityFactor = (r != 0) ? Math.abs(x) / r : Double.POSITIVE_INFINITY;
        results.put(DerivateResistance.QUALITY_FACTOR, qualityFactor);

        // 4. Loss Factor / Dissipation (D) = R / |X|
        double lossFactor = (x != 0) ? r / Math.abs(x) : Double.POSITIVE_INFINITY;
        results.put(DerivateResistance.LOSS_FACTOR, lossFactor);

        // Parasitic element analysis based on frequency bounds and reactive load polarities
        if (frequency > 0 && x != 0) {
            double angularFrequency = 2 * Math.PI * frequency;

            if (x > 0) {
                // 5. Parasitic Inductance (L) = X / (2 * pi * f)
                double L = x / angularFrequency;
                results.put(DerivateResistance.PARASITIC_INDUCTANCE, L);
                results.put(DerivateResistance.PARASITIC_CAPACITANCE, 0.0);
            } else {
                // 6. Parasitic Capacitance (C) = 1 / (2 * pi * f * |X|)
                double C = 1.0 / (angularFrequency * Math.abs(x));
                results.put(DerivateResistance.PARASITIC_CAPACITANCE, C);
                results.put(DerivateResistance.PARASITIC_INDUCTANCE, 0.0);
            }
        } else {
            // Default safe boundaries for hardware initialization states or zero lines
            results.put(DerivateResistance.PARASITIC_INDUCTANCE, 0.0);
            results.put(DerivateResistance.PARASITIC_CAPACITANCE, 0.0);
        }

        return results;
    }
}
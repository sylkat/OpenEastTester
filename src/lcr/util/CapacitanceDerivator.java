package lcr.util;

import lcr.enums.DerivateCapacitance;
import java.util.EnumMap;
import java.util.Map;

/**
 * Utility calculator processing secondary complex electrical parameters derived from
 * primary Capacitance (C) and Dissipation Factor (D) metrics under dynamic AC test frequencies.
 * * @author sylkat
 */
public class CapacitanceDerivator {

    /**
     * Calculates the complete structural mapping matrix of secondary derived parameter assets
     * based on primary capacitance, dissipation factor loss loops, and operational signal frequencies.
     * * @param c         the primary measured capacitance magnitude value in Farads (F)
     * @param d         the dimensionless dissipation or loss factor coefficient value
     * @param frequency the nominal hardware AC test signal operational frequency value in Hertz (Hz)
     * @return a structured EnumMap containing all successfully resolved complex derivative measurements
     */
    public static Map<DerivateCapacitance, Double> calculate(double c, double d, double frequency) {
        Map<DerivateCapacitance, Double> results = new EnumMap<>(DerivateCapacitance.class);

        if (frequency > 0 && c != 0) {
            double angularFrequency = 2 * Math.PI * frequency;

            // 1. Capacitive Reactance (X) = -1 / (2 * pi * f * C)
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

            // 5. Quality Factor (Q) = 1 / D
            double qualityFactor = (d != 0) ? 1.0 / d : Double.POSITIVE_INFINITY;
            results.put(DerivateCapacitance.QUALITY_FACTOR, qualityFactor);

        } else {
            // Default safe boundaries for hardware initialization states or zero lines
            results.put(DerivateCapacitance.REACTANCE, 0.0);
            results.put(DerivateCapacitance.EQUIVALENT_SERIES_RESISTANCE, 0.0);
            results.put(DerivateCapacitance.IMPEDANCE, 0.0);
            results.put(DerivateCapacitance.PHASE_ANGLE, -90.0);
            results.put(DerivateCapacitance.QUALITY_FACTOR, 0.0);
        }

        return results;
    }
}
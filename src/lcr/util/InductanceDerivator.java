package lcr.util;

import lcr.enums.DerivateInductance;
import java.util.EnumMap;
import java.util.Map;

/**
 * Utility calculator processing secondary complex electrical parameters derived from
 * primary Inductance (L) and Quality Factor (Q) metrics under dynamic AC test frequencies.
 * Accommodates standard inductors as well as capacitive behavior trends.
 * * @author sylkat
 */
public class InductanceDerivator {

    /**
     * Calculates the complete structural mapping matrix of secondary derived parameter assets
     * based on primary inductance, quality factor vectors, and operational signal frequencies.
     * * @param l         the primary measured inductance magnitude value in Henries (H), can be negative if capacitive
     * @param q         the dimensionless quality factor coefficient value
     * @param frequency the nominal hardware AC test signal operational frequency value in Hertz (Hz)
     * @return a structured EnumMap containing all successfully resolved complex derivative measurements
     */
    public static Map<DerivateInductance, Double> calculate(double l, double q, double frequency) {
        Map<DerivateInductance, Double> results = new EnumMap<>(DerivateInductance.class);

        // Allow negative 'l' values (capacitive behaviors), only reject exact zero lines
        if (frequency > 0 && l != 0) {
            double angularFrequency = 2 * Math.PI * frequency;

            // 1. Reactance (X) = 2 * pi * f * L
            double reactance = angularFrequency * l;
            results.put(DerivateInductance.REACTANCE, reactance);

            // 2. Equivalent Series Resistance (ESR) = |X| / |Q|
            double absQ = Math.abs(q);
            double esr = (absQ != 0) ? Math.abs(reactance) / absQ : Double.POSITIVE_INFINITY;
            results.put(DerivateInductance.SERIES_RESISTANCE, esr);

            // 3. Total Impedance (Z) = sqrt(ESR² + X²)
            double impedance = Double.isInfinite(esr) ? Double.POSITIVE_INFINITY : Math.sqrt(Math.pow(esr, 2) + Math.pow(reactance, 2));
            results.put(DerivateInductance.IMPEDANCE, impedance);

            // 4. Phase Angle (THR) in degrees = atan2(X, ESR)
            double phaseAngle = Math.toDegrees(Math.atan2(reactance, esr));
            results.put(DerivateInductance.PHASE_ANGLE, phaseAngle);

            // 5. Loss Factor / Dissipation (D) = 1 / |Q|
            double lossFactor = (absQ != 0) ? 1.0 / absQ : Double.POSITIVE_INFINITY;
            results.put(DerivateInductance.LOSS_FACTOR, lossFactor);

        } else {
            // Default safe boundaries for hardware initialization states or zero lines
            results.put(DerivateInductance.REACTANCE, 0.0);
            results.put(DerivateInductance.SERIES_RESISTANCE, 0.0);
            results.put(DerivateInductance.IMPEDANCE, 0.0);
            results.put(DerivateInductance.PHASE_ANGLE, 0.0);
            results.put(DerivateInductance.LOSS_FACTOR, 0.0);
        }

        return results;
    }
}
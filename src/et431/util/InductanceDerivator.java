package et431.util;

import et431.enums.DerivateInductance;

import java.util.EnumMap;
import java.util.Map;

public class InductanceDerivator {

    /**
     * Calculates all derived parameters based on L, Q, and the test frequency.
     * Works for both true inductors (L > 0) and capacitive behaviors (L < 0).
     *
     * @param l In henries (Inductance, can be negative if capacitive)
     * @param q Quality factor (dimensionless)
     * @param frequency In Hz (e.g., 1000 for 1kHz).
     * @return An EnumMap containing all the calculated derived parameters.
     */
    public static Map<DerivateInductance, Double> calculate(double l, double q, double frequency) {
        Map<DerivateInductance, Double> results = new EnumMap<>(DerivateInductance.class);

        // CORRECCIÓN 1: Permitir valores negativos de 'l' (condensadores), solo rechazar el 0 exacto
        if (frequency > 0 && l != 0) {
            double angularFrequency = 2 * Math.PI * frequency;

            // 1. Reactancia (X) = 2 * pi * f * L
            // Si 'l' es -2.5, esto dará tu valor negativo real (ej. -1633.0 Ohm)
            double reactance = angularFrequency * l;
            results.put(DerivateInductance.REACTANCE, reactance);

            // 2. Resistencia Serie Equivalente (ESR) = |X| / |Q|
            // La resistencia siempre es positiva. Usamos Math.abs para asegurar consistencia
            double absQ = Math.abs(q);
            double esr = (absQ != 0) ? Math.abs(reactance) / absQ : Double.POSITIVE_INFINITY;
            results.put(DerivateInductance.SERIES_RESISTANCE, esr);

            // 3. Impedancia Total (Z) = sqrt(ESR² + X²)
            // Al elevar al cuadrado, el signo menos de la reactancia se gestiona solo de forma segura
            double impedance = Double.isInfinite(esr) ? Double.POSITIVE_INFINITY : Math.sqrt(Math.pow(esr, 2) + Math.pow(reactance, 2));
            results.put(DerivateInductance.IMPEDANCE, impedance);

            // 4. Ángulo de Fase (THR) en grados = atan2(X, ESR)
            // CORRECCIÓN 2: Al ser 'reactance' negativa, atan2 devuelve el ángulo negativo real (ej. -90.0°)
            double phaseAngle = Math.toDegrees(Math.atan2(reactance, esr));
            results.put(DerivateInductance.PHASE_ANGLE, phaseAngle);

            // 5. Factor de Pérdidas / Disipación (D) = 1 / |Q|
            // El factor D siempre es positivo
            double lossFactor = (absQ != 0) ? 1.0 / absQ : Double.POSITIVE_INFINITY;
            results.put(DerivateInductance.LOSS_FACTOR, lossFactor);

        } else {
            // Valores por defecto seguros si los parámetros de entrada son exactamente cero o inválidos
            results.put(DerivateInductance.REACTANCE, 0.0);
            results.put(DerivateInductance.SERIES_RESISTANCE, 0.0);
            results.put(DerivateInductance.IMPEDANCE, 0.0);
            results.put(DerivateInductance.PHASE_ANGLE, 0.0); // Cambiado a 0.0 por seguridad en DC/Invalido
            results.put(DerivateInductance.LOSS_FACTOR, 0.0);
        }

        return results;
    }
}
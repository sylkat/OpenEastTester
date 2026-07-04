package et431.beans;

import et431.enums.*;

import java.util.Map;

public class MeasurementDTO {
    private final String measureType;
    private final String typeA;
    private final String valueA;
    private final String typeB;
    private final String valueB;
    private final SeriesMode seriesMode;
    private final Map<DerivateResistance, Double> resistanceDerivator;
    private final Map<DerivateCapacitance, Double> capacitanceDerivator;
    private final Map<DerivateInductance, Double> inductanceDerivator;
    private final Map<DerivateImpedance, Double> impedanceDerivator;

    public MeasurementDTO(String measureType,
                          String typeA,
                          String valueA,
                          String typeB,
                          String valueB,
                          SeriesMode seriesMode,
                          Map<DerivateResistance, Double> resistanceDerivator,
                          Map<DerivateCapacitance, Double> capacitanceDerivator,
                          Map<DerivateInductance, Double> inductanceDerivator,
                          Map<DerivateImpedance, Double> impedanceDerivator) {
        this.measureType=measureType;
        this.typeA = typeA;
        this.valueA = valueA;
        this.typeB = typeB;
        this.valueB = valueB;
        this.seriesMode =seriesMode;
        this.resistanceDerivator=resistanceDerivator;
        this.capacitanceDerivator=capacitanceDerivator;
        this.inductanceDerivator=inductanceDerivator;
        this.impedanceDerivator=impedanceDerivator;
    }
    public String getMeasureType() { return measureType; }
    public String getTypeA() { return typeA; }
    public String getValueA() { return valueA; }
    public String getTypeB() { return typeB; }
    public String getValueB() { return valueB; }

    public SeriesMode getSeriesMode() {
        return seriesMode;
    }

    public Map<DerivateResistance, Double> getResistanceDerivator() {
        return resistanceDerivator;
    }

    public Map<DerivateCapacitance, Double> getCapacitanceDerivator() {
        return capacitanceDerivator;
    }

    public Map<DerivateInductance, Double> getInductanceDerivator() {
        return inductanceDerivator;
    }

    public Map<DerivateImpedance, Double> getImpedanceDerivator() {
        return impedanceDerivator;
    }
}
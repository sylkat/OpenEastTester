package lcr.beans;

import lcr.enums.*;

import java.util.Map;

public class MeasurementDTO {
    private final String realValueA;
    private final String realValueB;
    private final String measureType;
    private final String typeA;
    private final String valueA;
    private final String typeB;
    private final String valueB;
    private final SeriesMode seriesMode;
    private final Boolean connected;
    private final Map<DerivateResistance, Double> resistanceDerivator;
    private final Map<DerivateCapacitance, Double> capacitanceDerivator;
    private final Map<DerivateInductance, Double> inductanceDerivator;
    private final Map<DerivateImpedance, Double> impedanceDerivator;
    public MeasurementDTO(String realValueA,
                          String realValueB,
                          String measureType,
                          String typeA,
                          String valueA,
                          String typeB,
                          String valueB,
                          SeriesMode seriesMode,
                          Boolean connected,
                          Map<DerivateResistance, Double> resistanceDerivator,
                          Map<DerivateCapacitance, Double> capacitanceDerivator,
                          Map<DerivateInductance, Double> inductanceDerivator,
                          Map<DerivateImpedance, Double> impedanceDerivator) {
        this.realValueA=realValueA;
        this.realValueB=realValueB;
        this.measureType=measureType;
        this.typeA = typeA;
        this.valueA = valueA;
        this.typeB = typeB;
        this.valueB = valueB;
        this.seriesMode =seriesMode;
        this.connected = connected;
        this.resistanceDerivator=resistanceDerivator;
        this.capacitanceDerivator=capacitanceDerivator;
        this.inductanceDerivator=inductanceDerivator;
        this.impedanceDerivator=impedanceDerivator;
    }

    public String getRealValueA() {
        return realValueA;
    }
    public String getRealValueB() {
        return realValueB;
    }

    public String getMeasureType() { return measureType; }
    public String getTypeA() { return typeA; }
    public String getValueA() { return valueA; }
    public String getTypeB() { return typeB; }
    public String getValueB() { return valueB; }

    public SeriesMode getSeriesMode() {
        return seriesMode;
    }

    public Boolean getConnected() {
        return connected;
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
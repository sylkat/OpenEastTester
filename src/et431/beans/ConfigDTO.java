package et431.beans;

import et431.enums.*;

public class ConfigDTO {
    private  Frequency frequency;
    private  Voltage voltage;
    private  Aperture aperture;
    private  PrimaryParameter primaryMeasurement;
    private  SecondaryParameter secondaryMeasurement;
    private  boolean autoRange;
    private  Range range;
    private  BiasVoltage bias;
    private  SeriesMode seriesMode;

    public ConfigDTO(Frequency frequency, Voltage voltage, Aperture aperture,
                     PrimaryParameter primaryMeasurement, SecondaryParameter secondaryMeasurement,
                     boolean autoRange, Range range, BiasVoltage bias, SeriesMode seriesMode) {
        this.frequency = frequency;
        this.voltage = voltage;
        this.aperture = aperture;
        this.primaryMeasurement = primaryMeasurement;
        this.secondaryMeasurement = secondaryMeasurement;
        this.autoRange = autoRange;
        this.range = range;
        this.bias = bias;
        this.seriesMode = seriesMode;
    }

    public Frequency getFrequency() { return frequency; }
    public Voltage getVoltage() { return voltage; }
    public Aperture getAperture() { return aperture; }
    public PrimaryParameter getPrimaryMeasurement() { return primaryMeasurement; }
    public SecondaryParameter getSecondaryMeasurement() { return secondaryMeasurement; }
    public boolean isAutoRange() { return autoRange; }
    public Range getRange() { return range; }
    public BiasVoltage getBias() { return bias; }

    public SeriesMode getSeriesMode() {
        return seriesMode;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public void setVoltage(Voltage voltage) {
        this.voltage = voltage;
    }

    public void setAperture(Aperture aperture) {
        this.aperture = aperture;
    }

    public void setPrimaryMeasurement(PrimaryParameter primaryMeasurement) {
        this.primaryMeasurement = primaryMeasurement;
    }

    public void setSecondaryMeasurement(SecondaryParameter secondaryMeasurement) {
        this.secondaryMeasurement = secondaryMeasurement;
    }

    public void setAutoRange(boolean autoRange) {
        this.autoRange = autoRange;
    }

    public void setRange(Range range) {
        this.range = range;
    }

    public void setBias(BiasVoltage bias) {
        this.bias = bias;
    }

    public void setSeriesMode(SeriesMode seriesMode) {
        this.seriesMode = seriesMode;
    }
}
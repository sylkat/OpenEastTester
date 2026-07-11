package lcr.beans;

import lcr.enums.*;

/**
 * Data Transfer Object containing the hardware configuration parameters for the LCR meter.
 * * @author sylkat
 */
public class ConfigDTO {
    private Frequency frequency;
    private Voltage voltage;
    private Aperture aperture;
    private PrimaryParameter primaryMeasurement;
    private SecondaryParameter secondaryMeasurement;
    private boolean autoRange;
    private Range range;
    private BiasVoltage bias;
    private SeriesMode seriesMode;

    /**
     * Constructs a full configuration instance.
     * * @param frequency            the test frequency setting
     * @param voltage              the test signal voltage level
     * @param aperture             the measurement integration time/speed
     * @param primaryMeasurement   the primary parameter to measure (e.g., Cs, Cp)
     * @param secondaryMeasurement the secondary parameter to measure (e.g., D, Q, ESR)
     * @param autoRange            true to enable automatic range selection
     * @param range                the specific measurement impedance range
     * @param bias                 the DC bias voltage configuration
     * @param seriesMode           the equivalent circuit mode (Series/Parallel)
     */
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
    public SeriesMode getSeriesMode() { return seriesMode; }

    public void setFrequency(Frequency frequency) { this.frequency = frequency; }
    public void setVoltage(Voltage voltage) { this.voltage = voltage; }
    public void setAperture(Aperture aperture) { this.aperture = aperture; }
    public void setPrimaryMeasurement(PrimaryParameter primaryMeasurement) { this.primaryMeasurement = primaryMeasurement; }
    public void setSecondaryMeasurement(SecondaryParameter secondaryMeasurement) { this.secondaryMeasurement = secondaryMeasurement; }
    public void setAutoRange(boolean autoRange) { this.autoRange = autoRange; }
    public void setRange(Range range) { this.range = range; }
    public void setBias(BiasVoltage bias) { this.bias = bias; }
    public void setSeriesMode(SeriesMode seriesMode) { this.seriesMode = seriesMode; }
}
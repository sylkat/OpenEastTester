package et431.business;

import et431.beans.DeviceInfo;
import et431.beans.Measurement;
import et431.enums.*;
import et431.ET431Exception;

public interface LcrMeter {

    // -------------------------------------------------------
    // Connection
    // -------------------------------------------------------
    void connect() throws ET431Exception;
    void disconnect();
    boolean isConnected();

    // -------------------------------------------------------
    // Raw SCPI
    // -------------------------------------------------------
    String execute(String command) throws Exception;
    DeviceInfo getDeviceInfo() throws Exception;

    // -------------------------------------------------------
    // Measurement
    // -------------------------------------------------------
    Measurement fetch() throws Exception;

    // -------------------------------------------------------
    // Frequency
    // -------------------------------------------------------
    Frequency getFrequency() throws Exception;
    void setFrequency(Frequency frequency) throws Exception;

    // -------------------------------------------------------
    // Voltage
    // -------------------------------------------------------
    Voltage getVoltage() throws Exception;
    void setVoltage(Voltage voltage) throws Exception;

    // -------------------------------------------------------
    // Aperture
    // -------------------------------------------------------
    Aperture getAperture() throws Exception;
    void setAperture(Aperture aperture) throws Exception;

    // -------------------------------------------------------
    // Primary Parameter
    // -------------------------------------------------------
    PrimaryParameter getPrimaryParameter() throws Exception;
    void setPrimaryParameter(PrimaryParameter parameter) throws Exception;

    // -------------------------------------------------------
    // Secondary Parameter
    // -------------------------------------------------------
    SecondaryParameter getSecondaryParameter() throws Exception;
    void setSecondaryParameter(SecondaryParameter parameter) throws Exception;

    // -------------------------------------------------------
    // Series / Parallel
    // -------------------------------------------------------
    SeriesMode getSeriesMode() throws Exception;
    void setSeriesMode(SeriesMode mode) throws Exception;

    // -------------------------------------------------------
    // Auto Range
    // -------------------------------------------------------
    boolean isAutoRange() throws Exception;
    void setAutoRange(boolean enabled) throws Exception;

    // -------------------------------------------------------
    // Manual Range
    // -------------------------------------------------------
    Range getRange() throws Exception;
    void setRange(Range range) throws Exception;

    // -------------------------------------------------------
    // Bias Voltage
    // -------------------------------------------------------
    BiasVoltage getBiasVoltage() throws Exception;
    void setBiasVoltage(BiasVoltage bias) throws Exception;

}
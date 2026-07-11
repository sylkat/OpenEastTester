package lcr.business;

import lcr.beans.DeviceInfo;
import lcr.beans.Measurement;
import lcr.enums.*;
import lcr.ET431Exception;

/**
 * Unified interface definition for LCR meter hardware control, exposing
 * connectivity, raw SCPI execution, and device configuration parameters.
 * * @author sylkat
 */
public interface LcrMeter {

    /**
     * Establishes a connection to the hardware meter.
     * * @throws ET431Exception if the connection fails or initialization errors occur
     */
    void connect() throws ET431Exception;

    /**
     * Closes the connection to the hardware meter.
     */
    void disconnect();

    /**
     * Checks the active connection status of the device.
     * * @return true if the serial channel is open
     */
    boolean isConnected();

    /**
     * Sends a raw SCPI command to the meter and reads its string response.
     * * @param command the raw text command payload
     * @return the device response string
     * @throws Exception if serial transport or execution fails
     */
    String execute(String command) throws Exception;

    /**
     * Queries and parses identification info string from the instrument.
     * * @return a DeviceInfo container instance
     * @throws Exception if parsing or hardware handshake fails
     */
    DeviceInfo getDeviceInfo() throws Exception;

    /**
     * Triggers and fetches a single measurement sequence reading frame.
     * * @return a new Measurement instance containing raw read values
     * @throws Exception if data retrieval drops or fails timeout bounds
     */
    Measurement fetch() throws Exception;

    Frequency getFrequency() throws Exception;
    void setFrequency(Frequency frequency) throws Exception;

    Voltage getVoltage() throws Exception;
    void setVoltage(Voltage voltage) throws Exception;

    Aperture getAperture() throws Exception;
    void setAperture(Aperture aperture) throws Exception;

    PrimaryParameter getPrimaryParameter() throws Exception;
    void setPrimaryParameter(PrimaryParameter parameter) throws Exception;

    SecondaryParameter getSecondaryParameter() throws Exception;
    void setSecondaryParameter(SecondaryParameter parameter) throws Exception;

    SeriesMode getSeriesMode() throws Exception;
    void setSeriesMode(SeriesMode mode) throws Exception;

    boolean isAutoRange() throws Exception;
    void setAutoRange(boolean enabled) throws Exception;

    /**
     * Retrieves the underlying low-level serial link wrapper.
     * * @return the active SerialConnection instance reference
     */
    SerialConnection getSerialConnection();

    Range getRange() throws Exception;
    void setRange(Range range) throws Exception;

    BiasVoltage getBiasVoltage() throws Exception;
    void setBiasVoltage(BiasVoltage bias) throws Exception;
}
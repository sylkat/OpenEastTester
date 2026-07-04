package et431.business;

import et431.beans.DeviceInfo;
import et431.ET431Exception;
import et431.beans.Measurement;
import et431.enums.*;

public class ET431 {

    private final SerialConnection serial;

    public ET431(String portName) {
        serial = new SerialConnection(portName);
    }

    // -------------------------------------------------------
    // Connection
    // -------------------------------------------------------

    public void connect() throws ET431Exception {
        serial.connect();
    }

    public void disconnect() {
        serial.disconnect();
    }

    public boolean isConnected() {
        return serial.isConnected();
    }

    // -------------------------------------------------------
    // Raw SCPI
    // -------------------------------------------------------

    public String execute(String command) throws Exception {
        return serial.execute(command);
    }

    public DeviceInfo getDeviceInfo() throws Exception {
        String response = serial.execute("*IDN?");
        if (response == null)
            throw new ET431Exception("Invalid device information.");
        String[] values = response.split(",");
        if (values.length < 5)
            throw new ET431Exception("Invalid response: " + response);
        return new DeviceInfo(
                values[0],
                values[1],
                values[2],
                values[4]
        );
    }

    // -------------------------------------------------------
    // Measurement
    // -------------------------------------------------------

    public Measurement fetch() throws Exception {
        String response = serial.execute("FETCH?");
        if (response == null)
            throw new ET431Exception("Invalid measurement.");
        String[] values = response.split(",");
        if (values.length != 2)
            throw new ET431Exception("Invalid measurement: " + response);
         return new Measurement(
                Double.parseDouble(values[0]),
                Double.parseDouble(values[1])
        );
    }

    // -------------------------------------------------------
    // Frequency
    // -------------------------------------------------------

    public Frequency getFrequency() throws Exception {
        String response = serial.execute("FREQ?").trim();
        double parsedValue = Double.parseDouble(response);
        int hz = (int) Math.round(parsedValue);
        return Frequency.fromValue(hz);
    }

    public void setFrequency(Frequency frequency) throws Exception {
        serial.execute("FREQ " + frequency.getValue());
    }

    // -------------------------------------------------------
    // Voltage
    // -------------------------------------------------------

    public Voltage getVoltage() throws Exception {
        String response = serial.execute("VOLT?").trim();
        double parsedValue = Double.parseDouble(response);
        int mv = (int) Math.round(parsedValue);
        return Voltage.fromValue(mv);
    }
    public void setVoltage(Voltage voltage) throws Exception {
        serial.execute("VOLT " + voltage.getValue());
    }

    // -------------------------------------------------------
    // Aperture
    // -------------------------------------------------------

    public Aperture getAperture() throws Exception {
        String response = serial.execute("APER?");
        return Aperture.fromString(response);
    }

    public void setAperture(Aperture aperture) throws Exception {
        serial.execute("APER " + aperture.name());
    }

    // -------------------------------------------------------
    // Primary Parameter
    // -------------------------------------------------------

    public PrimaryParameter getPrimaryParameter() throws Exception {
        return PrimaryParameter.valueOf(serial.execute("FUNC:IMP:A?"));
    }

    public void setPrimaryParameter(PrimaryParameter parameter) throws Exception {
        serial.execute("FUNC:IMP:A " + parameter.name());
    }

    // -------------------------------------------------------
    // Secondary Parameter
    // -------------------------------------------------------

    public SecondaryParameter getSecondaryParameter() throws Exception {
        return SecondaryParameter.valueOf(serial.execute("FUNC:IMP:B?"));
    }

    public void setSecondaryParameter(SecondaryParameter parameter) throws Exception {
        serial.execute("FUNC:IMP:B " + parameter.name());
    }

    // -------------------------------------------------------
    // Series / Parallel
    // -------------------------------------------------------

    public SeriesMode getSeriesMode() throws Exception {
        String value = serial.execute("FUNC:IMP:EQU?");
        if (value.startsWith("SER"))
            return SeriesMode.Series;
        return SeriesMode.Parallel;
    }

    public void setSeriesMode(SeriesMode mode) throws Exception {
        serial.execute("FUNC:IMP:EQU " + mode.name());
    }

    // -------------------------------------------------------
    // Auto Range
    // -------------------------------------------------------

    public boolean isAutoRange() throws Exception {
        String response=serial.execute("FUNC:IMP:RANG:AUTO?");
        return response.equalsIgnoreCase("ON");
    }

    public void setAutoRange(boolean enabled) throws Exception {
        serial.execute("FUNC:IMP:RANG:AUTO " + (enabled ? "ON" : "OFF"));
    }

    // -------------------------------------------------------
    // Manual Range
    // -------------------------------------------------------

    public Range getRange() throws Exception {
        int value = Integer.parseInt(serial.execute("FUNC:IMP:RANG?"));
        for (Range r : Range.values()) {
            if (r.getValue() == value)
                return r;
        }
        return null;
    }

    public void setRange(Range range) throws Exception {
        serial.execute("FUNC:IMP:RANG " + range.getValue());
    }

    // -------------------------------------------------------
    // Relative Mode
    // -------------------------------------------------------

    public RelativeMode getRelativeMode() throws Exception {
        return RelativeMode.valueOf(serial.execute("FUNC:DEV:MODE?"));
    }

    public void setRelativeMode(RelativeMode mode) throws Exception {
        serial.execute("FUNC:DEV:MODE " + mode.name());
    }

    // -------------------------------------------------------
    // Bias Voltage
    // -------------------------------------------------------

    public BiasVoltage getBiasVoltage() throws Exception {
        int value = Integer.parseInt(serial.execute("BIAS:VOLT?"));
        for (BiasVoltage b : BiasVoltage.values()) {
            if (b.getValue() == value)
                return b;
        }
        return null;
    }

    public void setBiasVoltage(BiasVoltage bias) throws Exception {
        serial.execute("BIAS:VOLT " + bias.getValue());
    }

    // -------------------------------------------------------
    // Display
    // -------------------------------------------------------

    public DisplayPage getDisplayPage() throws Exception {
        String page = serial.execute("DISP:PAGE?");
        if (page.startsWith("MEAS"))
            return DisplayPage.MEAS;
        return DisplayPage.SYST;
    }

    public void setDisplayPage(DisplayPage page) throws Exception {
        serial.execute("DISP:PAGE " + page.name());
    }
}
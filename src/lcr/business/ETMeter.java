package lcr.business;

import lcr.beans.DeviceInfo;
import lcr.ET431Exception;
import lcr.beans.Measurement;
import lcr.enums.*;
import lcr.util.Constants;

public class ETMeter implements LcrMeter {

    private final SerialConnection serial;

    public ETMeter(String portName) {
        serial = new SerialConnection(portName);
    }

    // -------------------------------------------------------
    // Connection
    // -------------------------------------------------------
    @Override
    public void connect() throws ET431Exception {
        serial.connect();
    }
    @Override
    public void disconnect() {
        serial.disconnect();
    }
    @Override
    public boolean isConnected() {
        return serial.isConnected();
    }

    // -------------------------------------------------------
    // Raw SCPI
    // -------------------------------------------------------
    @Override
    public String execute(String command) throws Exception {
        return serial.execute(command);
    }
    @Override
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
    @Override
    public Measurement fetch() throws Exception {
        String response = serial.execute("FETCH?");
        if (response == null) {
            if (Constants.DEBUG > 3) {
                throw new ET431Exception("Invalid measurement.");
            }
            return new Measurement(
                    Double.parseDouble("0"),
                    Double.parseDouble("0"));
        }
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
    @Override
    public Frequency getFrequency() throws Exception {
        String response = serial.execute("FREQ?").trim();
        double parsedValue = Double.parseDouble(response);
        int hz = (int) Math.round(parsedValue);
        return Frequency.fromValue(hz);
    }
    @Override
    public void setFrequency(Frequency frequency) throws Exception {
        serial.execute("FREQ " + frequency.getValue());
    }

    // -------------------------------------------------------
    // Voltage
    // -------------------------------------------------------
    @Override
    public Voltage getVoltage() throws Exception {
        String response = serial.execute("VOLT?").trim();
        double parsedValue = Double.parseDouble(response);
        int mv = (int) Math.round(parsedValue);
        return Voltage.fromValue(mv);
    }
    @Override
    public void setVoltage(Voltage voltage) throws Exception {
        serial.execute("VOLT " + voltage.getValue());
    }

    // -------------------------------------------------------
    // Aperture
    // -------------------------------------------------------
    @Override
    public Aperture getAperture() throws Exception {
        String response = serial.execute("APER?");
        return Aperture.fromString(response);
    }
    @Override
    public void setAperture(Aperture aperture) throws Exception {
        serial.execute("APER " + aperture.name());
    }

    // -------------------------------------------------------
    // Primary Parameter
    // -------------------------------------------------------
    @Override
    public PrimaryParameter getPrimaryParameter() throws Exception {
        return PrimaryParameter.valueOf(serial.execute("FUNC:IMP:A?"));
    }
    @Override
    public void setPrimaryParameter(PrimaryParameter parameter) throws Exception {
        serial.execute("FUNC:IMP:A " + parameter.name());
    }

    // -------------------------------------------------------
    // Secondary Parameter
    // -------------------------------------------------------
    @Override
    public SecondaryParameter getSecondaryParameter() throws Exception {
        return SecondaryParameter.valueOf(serial.execute("FUNC:IMP:B?"));
    }
    @Override
    public void setSecondaryParameter(SecondaryParameter parameter) throws Exception {
        serial.execute("FUNC:IMP:B " + parameter.name());
    }

    // -------------------------------------------------------
    // Series / Parallel
    // -------------------------------------------------------
    @Override
    public SeriesMode getSeriesMode() throws Exception {
        String value = serial.execute("FUNC:IMP:EQU?");
        if (value.startsWith("SER"))
            return SeriesMode.SER;
        return SeriesMode.PAL;
    }
    @Override
    public void setSeriesMode(SeriesMode mode) throws Exception {
        serial.execute("FUNC:IMP:EQU " + mode.name());
    }

    // -------------------------------------------------------
    // Auto Range
    // -------------------------------------------------------
    @Override
    public boolean isAutoRange() throws Exception {
        String response=serial.execute("FUNC:IMP:RANG:AUTO?");
        return response.equalsIgnoreCase("ON");
    }
    @Override
    public void setAutoRange(boolean enabled) throws Exception {
        serial.execute("FUNC:IMP:RANG:AUTO " + (enabled ? "ON" : "OFF"));
    }

    // -------------------------------------------------------
    // Manual Range
    // -------------------------------------------------------
    @Override
    public Range getRange() throws Exception {
        int value = Integer.parseInt(serial.execute("FUNC:IMP:RANG?"));
        for (Range r : Range.values()) {
            if (r.getValue() == value)
                return r;
        }
        return null;
    }
    @Override
    public void setRange(Range range) throws Exception {
        serial.execute("FUNC:IMP:RANG " + range.getValue());
    }

    // -------------------------------------------------------
    // Bias Voltage
    // -------------------------------------------------------
    @Override
    public BiasVoltage getBiasVoltage() throws Exception {
        int value = Integer.parseInt(serial.execute("BIAS:VOLT?"));
        for (BiasVoltage b : BiasVoltage.values()) {
            if (b.getValue() == value)
                return b;
        }
        return null;
    }
    @Override
    public void setBiasVoltage(BiasVoltage bias) throws Exception {
        serial.execute("BIAS:VOLT " + bias.getValue());
    }

//    // -------------------------------------------------------
//    // Display
//    // -------------------------------------------------------
//    @Override
//    public DisplayPage getDisplayPage() throws Exception {
//        String page = serial.execute("DISP:PAGE?");
//        if (page.startsWith("MEAS"))
//            return DisplayPage.MEAS;
//        return DisplayPage.SYST;
//    }
//    @Override
//    public void setDisplayPage(DisplayPage page) throws Exception {
//        serial.execute("DISP:PAGE " + page.name());
//    }
}
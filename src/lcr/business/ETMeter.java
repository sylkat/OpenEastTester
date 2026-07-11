package lcr.business;

import lcr.beans.DeviceInfo;
import lcr.ET431Exception;
import lcr.beans.Measurement;
import lcr.enums.*;
import lcr.util.Constants;

/**
 * Implementation of the LcrMeter interface handling SCPI command communication
 * over a serial connection for the ET431 series hardware.
 * * @author sylkat
 */
public class ETMeter implements LcrMeter {

    public final SerialConnection serial;

    /**
     * Initializes the meter with a target serial port name.
     * * @param portName the OS identifier for the communication port
     */
    public ETMeter(String portName) {
        serial = new SerialConnection(portName);
    }

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

    @Override
    public String execute(String command) throws Exception {
        return serial.execute(command);
    }

    @Override
    public DeviceInfo getDeviceInfo() throws Exception {
        String response = serial.execute("*IDN?");
        if (response == null) {
            throw new ET431Exception("Invalid device information.");
        }
        String[] values = response.split(",");
        if (values.length < 5) {
            throw new ET431Exception("Invalid response: " + response);
        }
        return new DeviceInfo(values[0], values[1], values[2], values[4]);
    }

    @Override
    public Measurement fetch() throws Exception {
        String response = serial.execute("FETCH?");
        if (response == null) {
            if (Constants.DEBUG > 3) {
                throw new ET431Exception("Invalid measurement.");
            }
            return new Measurement(0.0, 0.0);
        }
        String[] values = response.split(",");
        if (values.length != 2) {
            throw new ET431Exception("Invalid measurement: " + response);
        }
        return new Measurement(Double.parseDouble(values[0]), Double.parseDouble(values[1]));
    }

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

    @Override
    public Aperture getAperture() throws Exception {
        String response = serial.execute("APER?");
        return Aperture.fromString(response);
    }

    @Override
    public void setAperture(Aperture aperture) throws Exception {
        serial.execute("APER " + aperture.name());
    }

    @Override
    public PrimaryParameter getPrimaryParameter() throws Exception {
        String result = serial.execute("FUNC:IMP:A?");
        return PrimaryParameter.valueOf(result.trim());
    }

    @Override
    public void setPrimaryParameter(PrimaryParameter parameter) throws Exception {
        serial.execute("FUNC:IMP:A " + parameter.name());
    }

    @Override
    public SecondaryParameter getSecondaryParameter() throws Exception {
        return SecondaryParameter.valueOf(serial.execute("FUNC:IMP:B?"));
    }

    @Override
    public void setSecondaryParameter(SecondaryParameter parameter) throws Exception {
        serial.execute("FUNC:IMP:B " + parameter.name());
    }

    @Override
    public SeriesMode getSeriesMode() throws Exception {
        String value = serial.execute("FUNC:IMP:EQU?");
        if (value.startsWith("SER")) {
            return SeriesMode.SER;
        }
        return SeriesMode.PAL;
    }

    @Override
    public void setSeriesMode(SeriesMode mode) throws Exception {
        serial.execute("FUNC:IMP:EQU " + mode.name());
    }

    @Override
    public boolean isAutoRange() throws Exception {
        String response = serial.execute("FUNC:IMP:RANG:AUTO?");
        return response.equalsIgnoreCase("ON");
    }

    @Override
    public void setAutoRange(boolean enabled) throws Exception {
        serial.execute("FUNC:IMP:RANG:AUTO " + (enabled ? "ON" : "OFF"));
    }

    @Override
    public SerialConnection getSerialConnection() {
        return serial;
    }

    @Override
    public Range getRange() throws Exception {
        int value = Integer.parseInt(serial.execute("FUNC:IMP:RANG?"));
        for (Range r : Range.values()) {
            if (r.getValue() == value) {
                return r;
            }
        }
        return null;
    }

    @Override
    public void setRange(Range range) throws Exception {
        serial.execute("FUNC:IMP:RANG " + range.getValue());
    }

    @Override
    public BiasVoltage getBiasVoltage() throws Exception {
        int value = Integer.parseInt(serial.execute("BIAS:VOLT?"));
        for (BiasVoltage b : BiasVoltage.values()) {
            if (b.getValue() == value) {
                return b;
            }
        }
        return null;
    }

    @Override
    public void setBiasVoltage(BiasVoltage bias) throws Exception {
        serial.execute("BIAS:VOLT " + bias.getValue());
    }
}
package lcr.business;

import lcr.beans.DeviceInfo;
import lcr.ET431Exception;
import lcr.beans.Measurement;
import lcr.enums.*;

import static lcr.enums.PrimaryParameter.*;
import static lcr.enums.SecondaryParameter.ESR;

/**
 * Implementation of the LcrMeter interface handling SCPI command communication
 * over a serial connection for the Hioki IM3536 / 3532 series hardware.
 * * @author sylkat
 */
public class HiokiMeter implements LcrMeter {

    private final SerialConnection serial;

    /**
     * Initializes the meter with a target serial port name.
     * * @param portName the OS identifier for the communication port
     */
    public HiokiMeter(String portName) {
        this.serial = new SerialConnection(portName);
    }

    @Override
    public void connect() throws ET431Exception {
        serial.connect();
        try {
            serial.execute("*RST");
            serial.execute("MODE LCR");
            serial.execute("TRIG EXT");
            serial.execute("PAR1 CS");
            serial.execute("PAR2 RS");
            serial.execute("PAR3 PHASE");
            serial.execute("PAR4 Z");
        } catch (Exception e) {
            throw new ET431Exception("Error initializing Hioki IM3536: " + e.getMessage());
        }
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
        if (values.length < 4) {
            throw new ET431Exception("Invalid Hioki response: " + response);
        }

        return new DeviceInfo(
                values[0].trim(), // Manufacturer (HIOKI)
                values[1].trim(), // Model (e.g., IM3536)
                values[2].trim(), // Serial Number or Submodel
                values[3].trim()  // Firmware Version
        );
    }

    @Override
    public Measurement fetch() throws Exception {
        serial.execute("*CLS");
        serial.execute("*TRG");
        String response = serial.execute(":MEAS?");
        if (response == null) {
            throw new ET431Exception("Invalid measurement from Hioki.");
        }

        String[] values = response.split(",");
        if (values.length < 2) {
            throw new ET431Exception("Invalid measurement: " + response);
        }

        return new Measurement(
                Double.parseDouble(values[0].trim()),
                Double.parseDouble(values[1].trim())
        );
    }

    @Override
    public Frequency getFrequency() throws Exception {
        String response = serial.execute(":FREQuency?").trim();
        double parsedValue = Double.parseDouble(response);
        int hz = (int) Math.round(parsedValue);
        return Frequency.fromValue(hz);
    }

    @Override
    public void setFrequency(Frequency frequency) throws Exception {
        serial.execute(":FREQuency " + frequency.getValue());
    }

    @Override
    public Voltage getVoltage() throws Exception {
        String response = serial.execute(":LEVel:VOLTage?").trim();
        int mv = (int) Math.round(Double.parseDouble(response) * 1000.0);
        return Voltage.fromValue(mv);
    }

    @Override
    public void setVoltage(Voltage voltage) throws Exception {
        double volts = voltage.getValue() / 1000.0;
        serial.execute(":LEVel:VOLTage " + volts);
    }

    @Override
    public void setAperture(Aperture aperture) throws Exception {
        String speed;
        switch (aperture) {
            case FAST:
                speed = "FAST";
                break;
            case MED:
                speed = "NORMal";
                break;
            case SLOW:
                speed = "SLOW";
                break;
            case SLOW2:
                speed = "SLOW2";
                break;
            default:
                speed = "NORMal";
        }
        serial.execute(":SPEED " + speed);
    }

    @Override
    public Aperture getAperture() throws Exception {
        String response = serial.execute(":SPEED?").trim();
        return Aperture.fromString(response);
    }

    @Override
    public PrimaryParameter getPrimaryParameter() throws Exception {
        String response = serial.execute(":PARameter1?").trim().toUpperCase();
        if (response.equals("CS") || response.equals("CP")) {
            return C;
        } else if (response.equals("LS") || response.equals("LP")) {
            return L;
        } else if (response.equals("RS") || response.equals("RP")) {
            return R;
        } else if (response.equals("Z")) {
            return PrimaryParameter.Z;
        } else if (response.equals("RDC")) {
            return PrimaryParameter.DCR;
        }

        return PrimaryParameter.fromString(response);
    }

    @Override
    public void setPrimaryParameter(PrimaryParameter parameter) throws Exception {
        String hiokiParam;
        switch (parameter) {
            case C:
                hiokiParam = "CS";
                break;
            case L:
                hiokiParam = "LS";
                break;
            case R:
                hiokiParam = "RS";
                break;
            case Z:
                hiokiParam = "Z";
                break;
            case DCR:
                hiokiParam = "RDC";
                break;
            case ECAP:
                hiokiParam = "CS";
                break;
            case AUTO:
            default:
                throw new ET431Exception("Mode " + parameter + " is not natively supported on Hioki hardware.");
        }
        serial.execute(":PARameter1 " + hiokiParam);
    }

    @Override
    public SecondaryParameter getSecondaryParameter() throws Exception {
        String response = serial.execute(":PARameter2?").trim().toUpperCase();
        if (response.equals("RS")) {
            return ESR;
        }
        return SecondaryParameter.fromString(response);
    }

    @Override
    public void setSecondaryParameter(SecondaryParameter parameter) throws Exception {
        if (parameter == SecondaryParameter.THR) {
            throw new ET431Exception("THR is not supported by Hioki hardware.");
        }
        String hiokiParam = (parameter == ESR) ? "RS" : parameter.name();
        serial.execute(":PARameter2 " + hiokiParam);
    }

    @Override
    public SeriesMode getSeriesMode() throws Exception {
        return SeriesMode.SER;
    }

    @Override
    public void setSeriesMode(SeriesMode mode) throws Exception {
        throw new UnsupportedOperationException("Series/Parallel mode configuration is not supported by Hioki.");
    }

    @Override
    public boolean isAutoRange() throws Exception {
        String response = serial.execute(":RANGE:AUTO?").trim();
        return response.equalsIgnoreCase("ON") || response.equals("1");
    }

    @Override
    public void setAutoRange(boolean enabled) throws Exception {
        serial.execute(":RANGE:AUTO " + (enabled ? "ON" : "OFF"));
    }

    @Override
    public SerialConnection getSerialConnection() {
        return serial;
    }

    @Override
    public Range getRange() throws Exception {
        int value = Integer.parseInt(serial.execute(":RANGe?"));
        for (Range r : Range.values()) {
            if (r.getValue() == value) {
                return r;
            }
        }
        return null;
    }

    @Override
    public void setRange(Range range) throws Exception {
        serial.execute(":RANGe " + range.getValue());
    }

    @Override
    public BiasVoltage getBiasVoltage() throws Exception {
        return BiasVoltage.MV100;
    }

    @Override
    public void setBiasVoltage(BiasVoltage bias) throws Exception {
    }
}
package lcr.business;
/**
 * Mapea los Enums actuales al formato SCPI nativo y estricto de Tonghui:
 * CPD, CPQ, CPG, CPRP, CSD, CSQ, CSRS, LPQ, LPD, LPG, LPRD, LPRP, LSRD,
 * LSD, LSQ, LSRS, RX, ZTD, ZTR, GB, YTD, YTR, RPQ, RSQ, DCR
 */


import lcr.beans.DeviceInfo;
import lcr.ET431Exception;
import lcr.beans.Measurement;
import lcr.enums.*;
import lcr.util.Constants;

import static lcr.enums.PrimaryParameter.*;
import static lcr.enums.SecondaryParameter.ESR;
import static lcr.enums.SecondaryParameter.THR;

/**
 * Implementation of the LcrMeter interface handling strict composite SCPI command
 * mappings over a serial connection for Tonghui series hardware (e.g., TH2839).
 * * @author sylkat
 */
public class TonghuiMeter implements LcrMeter {

    private PrimaryParameter currentPrimary = PrimaryParameter.C;
    private SecondaryParameter currentSecondary = SecondaryParameter.D;
    private SeriesMode currentSeriesMode = SeriesMode.SER;
    private final SerialConnection serial;

    /**
     * Initializes the meter with a target serial port name.
     * * @param portName the OS identifier for the communication port
     */
    public TonghuiMeter(String portName) {
        this.serial = new SerialConnection(portName);
    }

    /**
     * Maps and commits the separate primary, secondary, and equivalent circuit mode
     * enums into a native, strict composite Tonghui SCPI function token.
     * * @throws Exception if the serial transport channel reports execution faults
     */
    private void updateTonghuiFunction() throws Exception {
        String combinedFunc;

        if (currentPrimary == PrimaryParameter.Z) {
            combinedFunc = "ZTD";
        } else if (currentPrimary == PrimaryParameter.DCR) {
            combinedFunc = "DCR";
        } else if (currentPrimary == PrimaryParameter.R) {
            if (currentSecondary == SecondaryParameter.X) {
                combinedFunc = "RX";
            } else {
                String modeStr = (currentSeriesMode == SeriesMode.SER) ? "SQ" : "PQ";
                combinedFunc = "R" + modeStr;
            }
        } else {
            String primStr = (currentPrimary == PrimaryParameter.L) ? "L" : "C";
            String modeStr = (currentSeriesMode == SeriesMode.SER) ? "S" : "P";
            String secStr;

            if (currentSecondary == SecondaryParameter.D) {
                secStr = "D";
            } else if (currentSecondary == SecondaryParameter.Q) {
                secStr = "Q";
            } else {
                secStr = (currentSeriesMode == SeriesMode.SER) ? "RS" : "RP";
            }

            combinedFunc = primStr + modeStr + secStr;
        }

        serial.execute(":FUNC:IMP " + combinedFunc);
    }

    @Override
    public void connect() throws ET431Exception {
        serial.connect();
        try {
            serial.execute("TRIG:SOUR INT");
        } catch (Exception e) {
            serial.disconnect();
            throw new ET431Exception("Error setting internal trigger: " + e.getMessage());
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

        response = response.replace("\"", "").trim();
        String[] values = response.split(",");
        if (values.length < 4) {
            throw new ET431Exception("Invalid response: " + response);
        }

        return new DeviceInfo(
                values[0],
                values[1],
                values[2],
                values.length > 3 ? values[3] : "UNKNOWN"
        );
    }

    @Override
    public Measurement fetch() throws Exception {
        String response = serial.execute("FETC?");
        if (response == null) {
            if (Constants.DEBUG > 3) {
                throw new ET431Exception("Invalid measurement.");
            }
            return new Measurement(0.0, 0.0);
        }

        response = response.replace("\"", "").trim();
        String[] values = response.split(",");
        if (values.length < 2) {
            throw new ET431Exception("Invalid measurement response: " + response);
        }

        if (values.length >= 3) {
            int status = Integer.parseInt(values[2].trim());
            if (status == -1 || status == 1 || status == 2) {
                throw new ET431Exception("Invalid measurement, status code from TH2839: " + status);
            }
        }

        return new Measurement(
                Double.parseDouble(values[0]),
                Double.parseDouble(values[1])
        );
    }

    @Override
    public Frequency getFrequency() throws Exception {
        String response = serial.execute("FREQ?");
        if (response == null) throw new ET431Exception("Timeout reading frequency.");

        response = response.trim().replace("\"", "");
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
        String response = serial.execute(":LEV:VOLT?");
        if (response == null) throw new ET431Exception("Timeout reading voltage.");

        response = response.trim().replace("\"", "");
        double parsedValue = Double.parseDouble(response);
        int mv = (int) Math.round(parsedValue * 1000);
        return Voltage.fromValue(mv);
    }

    @Override
    public void setVoltage(Voltage voltage) throws Exception {
        double volts = voltage.getValue() / 1000.0;
        serial.execute(":LEV:VOLT " + volts);
    }

    @Override
    public Aperture getAperture() throws Exception {
        String response = serial.execute("APER?");
        if (response == null) throw new ET431Exception("Timeout reading aperture.");
        response = response.trim().replace("\"", "");
        return Aperture.fromString(response);
    }

    @Override
    public void setAperture(Aperture aperture) throws Exception {
        serial.execute("APER " + aperture.name() + ", 4");
    }

    @Override
    public PrimaryParameter getPrimaryParameter() throws Exception {
        String response = serial.execute(":FUNC:IMP?");
        if (response == null) throw new ET431Exception("Timeout reading function.");

        response = response.trim().replace("\"", "").toUpperCase();
        if (response.startsWith("C")) return PrimaryParameter.C;
        if (response.startsWith("L")) return PrimaryParameter.L;
        if (response.startsWith("R")) return PrimaryParameter.R;
        if (response.startsWith("Z")) return PrimaryParameter.Z;
        return AUTO;
    }

    @Override
    public void setPrimaryParameter(PrimaryParameter parameter) throws Exception {
        this.currentPrimary = parameter;
        updateTonghuiFunction();
    }

    @Override
    public SecondaryParameter getSecondaryParameter() throws Exception {
        String response = serial.execute(":FUNC:IMP?");
        if (response == null) throw new ET431Exception("Invalid response.");

        response = response.trim().replace("\"", "").toUpperCase();

        if (response.startsWith("ZT")) {
            return THR;
        }
        if (response.equals("RX") || response.endsWith("X")) {
            return SecondaryParameter.X;
        }
        if (response.endsWith("D")) return SecondaryParameter.D;
        if (response.endsWith("Q")) return SecondaryParameter.Q;
        if (response.endsWith("RS") || response.endsWith("RP")) return ESR;

        return SecondaryParameter.THR;
    }

    @Override
    public void setSecondaryParameter(SecondaryParameter parameter) throws Exception {
        this.currentSecondary = parameter;
        updateTonghuiFunction();
    }

    @Override
    public SeriesMode getSeriesMode() throws Exception {
        String response = serial.execute(":FUNC:IMP?");
        if (response == null) throw new ET431Exception("Timeout reading equivalent mode.");

        response = response.trim().replace("\"", "").toUpperCase();
        if (response.contains("P")) {
            return SeriesMode.PAL;
        }
        return SeriesMode.SER;
    }

    @Override
    public void setSeriesMode(SeriesMode mode) throws Exception {
        this.currentSeriesMode = mode;
        updateTonghuiFunction();
    }

    @Override
    public boolean isAutoRange() throws Exception {
        String response = serial.execute("FUNC:IMP:RANG:AUTO?");
        if (response == null) throw new ET431Exception("Timeout reading auto range state.");
        response = response.replace("\"", "").trim();
        return response.equalsIgnoreCase("ON") || response.equals("1");
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
        String response = serial.execute("FUNC:IMP:RANG?");
        if (response == null) return null;

        response = response.replace("\"", "").trim();
        double parsedValue = Double.parseDouble(response);
        int value = (int) Math.round(parsedValue);

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
        String response = serial.execute("BIAS:VOLT?");
        if (response == null) return null;

        response = response.replace("\"", "").trim();
        double parsedValue = Double.parseDouble(response);
        int value = (int) Math.round(parsedValue);

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
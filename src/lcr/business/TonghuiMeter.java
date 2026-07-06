package lcr.business;

import lcr.beans.DeviceInfo;
import lcr.ET431Exception;
import lcr.beans.Measurement;
import lcr.enums.*;
import lcr.util.Constants;

import static lcr.enums.PrimaryParameter.*;
import static lcr.enums.SecondaryParameter.ESR;
import static lcr.enums.SecondaryParameter.THR;

public class TonghuiMeter implements LcrMeter {
    private PrimaryParameter currentPrimary = PrimaryParameter.C;
    private SecondaryParameter currentSecondary = SecondaryParameter.D;
    private SeriesMode currentSeriesMode = SeriesMode.SER;
    private final SerialConnection serial;

    public TonghuiMeter(String portName) {
        this.serial = new SerialConnection(portName);
    }

    /**
     * Mapea los Enums actuales al formato SCPI nativo y estricto de Tonghui:
     * CPD, CPQ, CPG, CPRP, CSD, CSQ, CSRS, LPQ, LPD, LPG, LPRD, LPRP, LSRD,
     * LSD, LSQ, LSRS, RX, ZTD, ZTR, GB, YTD, YTR, RPQ, RSQ, DCR
     */
    private void updateTonghuiFunction() throws Exception {
        String combinedFunc;
        if (currentPrimary == PrimaryParameter.Z) {
            combinedFunc = "ZTD";
        }
        else if (currentPrimary == PrimaryParameter.DCR) {
            combinedFunc = "DCR";
        }
        else if (currentPrimary == PrimaryParameter.R) { // <-- Evaluas el PRIMARIO primero
            if (currentSecondary == SecondaryParameter.X) {
                combinedFunc = "RX"; //
            } else {
                // Si el secundario es Q, D o cualquier otro, genera RSQ o RPQ de tu lista
                String modeStr = (currentSeriesMode == SeriesMode.SER) ? "SQ" : "PQ";
                combinedFunc = "R" + modeStr;
            }
        }else {
            String primStr;
            // Mapeo defensivo: Si es ECAP o AUTO, el medidor lo procesará físicamente como Capacitancia
            if (currentPrimary == PrimaryParameter.L) {
                primStr = "L";
            } else {
                primStr = "C"; // Para C, ECAP y AUTO
            }

            String modeStr = (currentSeriesMode == SeriesMode.SER) ? "S" : "P";
            String secStr;

            if (currentSecondary == SecondaryParameter.D) {
                secStr = "D";
            } else if (currentSecondary == SecondaryParameter.Q) {
                secStr = "Q";
            } else {
                // Si el secundario es ESR, X o cualquier otro valor en C o L,
                // la lista exige medir la resistencia equivalente (RS o RP)
                secStr = (currentSeriesMode == SeriesMode.SER) ? "RS" : "RP"; // Genera CSRS, CPRP, LSRS, LPRP
            }

            combinedFunc = primStr + modeStr + secStr;
        }

        serial.execute(":FUNC:IMP " + combinedFunc);
    }

    // -------------------------------------------------------
// Connection
// -------------------------------------------------------
    @Override
    public void connect() throws ET431Exception {
        serial.connect();
        try {
            serial.execute("TRIG:SOUR INT");
        } catch (Exception e) {
            serial.disconnect();
            throw new ET431Exception("Error setting intern trigger: " + e.getMessage());
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
        if (response == null) {
            throw new ET431Exception("Invalid device information.");
        }
        response = response.replace("\"", "").trim();
        String[] values = response.split(",");
        if (values.length < 4) { // Cambiado a 4, estándar SCPI mínimo suele ser Fabricante, Modelo, Serial, Firmware
            throw new ET431Exception("Invalid response: " + response);
        }
        return new DeviceInfo(
                values[0],
                values[1],
                values[2],
                values.length > 3 ? values[3] : "UNKNOWN"
        );
    }

    // -------------------------------------------------------
    // Measurement
    // -------------------------------------------------------
    @Override
    public Measurement fetch() throws Exception {
        String response = serial.execute("FETC?"); // Usamos la forma corta nativa del script
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
                throw new ET431Exception(
                        "Medición inválida, código de estado del TH2839: " + status);
            }
        }
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
        String response = serial.execute("FREQ?");
        if (response == null) throw new ET431Exception("Timeout reading frequency.");

        response = response.trim().replace("\"", "");
        double parsedValue = Double.parseDouble(response); // Seguro contra notación científica
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
        String response = serial.execute(":LEV:VOLT?");
        if (response == null) throw new ET431Exception("Timeout reading voltage.");

        response = response.trim().replace("\"", "");
        double parsedValue = Double.parseDouble(response); // Seguro contra notación científica (ej: 1.0000e+00)
        int mv = (int) Math.round(parsedValue * 1000);
        return Voltage.fromValue(mv);
    }

    @Override
    public void setVoltage(Voltage voltage) throws Exception {
        double volts = voltage.getValue() / 1000.0;
        serial.execute(":LEV:VOLT " + volts);
    }

    // -------------------------------------------------------
    // Aperture
    // -------------------------------------------------------
    @Override
    public Aperture getAperture() throws Exception {
        String response = serial.execute("APER?");
        if (response == null) throw new ET431Exception("Timeout reading aperture.");
        response = response.trim().replace("\"", "");
        return Aperture.fromString(response);
    }

    @Override
    public void setAperture(Aperture aperture) throws Exception {
        serial.execute("APER " + aperture.name()+", 4");
    }

    // -------------------------------------------------------
    // Primary Parameter
    // -------------------------------------------------------
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

    // -------------------------------------------------------
    // Secondary Parameter
    // -------------------------------------------------------
    @Override
    public SecondaryParameter getSecondaryParameter() throws Exception {
        String response = serial.execute(":FUNC:IMP?");
        if (response == null) throw new ET431Exception("Invalid response.");

        response = response.trim().replace("\"", "").toUpperCase();

        // Interceptamos ZT antes para evitar colisión con la 'D' de pérdidas
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

    // -------------------------------------------------------
    // Series / Parallel
    // -------------------------------------------------------
    @Override
    public SeriesMode getSeriesMode() throws Exception {
        String response = serial.execute(":FUNC:IMP?");
        if (response == null) throw new ET431Exception("Timeout reading equivalent mode.");

        response = response.trim().replace("\"", "").toUpperCase();
        // El modo está embebido en el segundo caracter de la respuesta de función (ej: CPRP o CPD -> P)
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

    // -------------------------------------------------------
    // Auto Range
    // -------------------------------------------------------
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

    // -------------------------------------------------------
    // Manual Range
    // -------------------------------------------------------
    @Override
    public Range getRange() throws Exception {
        String response = serial.execute("FUNC:IMP:RANG?");
        if (response == null) return null;

        response = response.replace("\"", "").trim();
        double parsedValue = Double.parseDouble(response); // Seguro contra notación científica
        int value = (int) Math.round(parsedValue);

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
        String response = serial.execute("BIAS:VOLT?");
        if (response == null) return null;

        response = response.replace("\"", "").trim();
        double parsedValue = Double.parseDouble(response); // Seguro contra notación científica
        int value = (int) Math.round(parsedValue);

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
}
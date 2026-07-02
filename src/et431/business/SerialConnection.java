package et431.business;

import com.fazecast.jSerialComm.SerialPort;
import et431.ET431Exception;

import java.nio.charset.StandardCharsets;

/**
 * Low-level serial communication layer for the LCR meter using jSerialComm.
 */
public class SerialConnection {

    private final SerialPort port;

    public SerialConnection(String portName) {
        port = SerialPort.getCommPort(portName);
        // Default hardware configuration parameters
        port.setBaudRate(115200);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.NO_PARITY);
        port.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 1000);
    }

    public void connect() throws ET431Exception {
        if (!port.openPort()) {
            throw new ET431Exception("Unable to open serial port.");
        }
    }

    public void disconnect() {
        if (port.isOpen()) {
            port.closePort();
        }
    }

    public boolean isConnected() {
        return port.isOpen();
    }

    public String execute(String command) throws Exception {
        flush(); // Clear any stale bytes before transmitting
        byte[] tx = (command + "\n").getBytes(StandardCharsets.US_ASCII);
        int written = port.writeBytes(tx, tx.length);
        if (written != tx.length) {
            throw new ET431Exception("Unable to send command: " + command);
        }
        Thread.sleep(100); // Wait for the device to respond
        int available = port.bytesAvailable();
        if (available <= 0) {
            return null;
        }
        byte[] buffer = new byte[available];
        port.readBytes(buffer, available);
        String response = new String(buffer, StandardCharsets.US_ASCII).trim();
        // SCPI-style error checking based on device return codes
        if (response.equalsIgnoreCase("cmd err")) {
            throw new ET431Exception("Command error: " + command);
        }
        if (response.equalsIgnoreCase("exec err")) {
            throw new ET431Exception("Execution error: " + command);
        }
        return response;
    }

    private void flush() {
        byte[] buffer = new byte[256];
        while (port.bytesAvailable() > 0) {
            int n = Math.min(port.bytesAvailable(), buffer.length);
            port.readBytes(buffer, n);
        }
    }
}
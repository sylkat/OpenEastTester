package lcr.business;

import com.fazecast.jSerialComm.SerialPort;
import lcr.ET431Exception;
import lcr.util.Log;

import java.nio.charset.StandardCharsets;

/**
 * Low-level serial communication infrastructure that wraps jSerialComm routines
 * to handle SCPI write/read transactions securely with the hardware meter.
 * * @author sylkat
 */
public class SerialConnection {

    private final SerialPort port;

    /**
     * Initializes a serial port representation with standard LCR instrument connection settings.
     * * @param portName the OS identifier for the communication channel port
     */
    public SerialConnection(String portName) {
        port = SerialPort.getCommPort(portName);
        port.setBaudRate(9600);
        port.setNumDataBits(8);
        port.setNumStopBits(SerialPort.ONE_STOP_BIT);
        port.setParity(SerialPort.NO_PARITY);
        port.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 1000);
    }

    /**
     * Attempts to establish an open serial link with a fallback retry attempt.
     * * @throws ET431Exception if structural errors occur or the descriptor cannot be claimed
     */
    public void connect() throws ET431Exception {
        try {
            if (!port.openPort()) {
                System.out.println("Failed to open port: " + port.getPortDescription() + " waiting for another attempt...");
                Thread.sleep(500);
                if (!port.openPort()) {
                    throw new ET431Exception("Unable to open serial port.");
                }
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new ET431Exception("Connection attempt was interrupted: " + ie.getMessage());
        } catch (Exception e) {
            if (e instanceof ET431Exception) {
                throw (ET431Exception) e;
            }
            throw new ET431Exception("Serial port error: " + e.getMessage());
        }
    }

    /**
     * Closes the underlying physical serial gateway frame channel.
     */
    public void disconnect() {
        if (port.isOpen()) {
            port.closePort();
        }
    }

    /**
     * Confirms the active state registry of the communication port handle.
     * * @return true if the serial port stream is currently active and open
     */
    public boolean isConnected() {
        return port.isOpen();
    }

    /**
     * Performs a synchronized thread-safe transaction sequence that flushes old traces,
     * transmits a text payload command, and tracks SCPI error returns.
     * * @param command the target instruction string payload to evaluate
     * @return the raw evaluation string response from the connected hardware device, or null if dead
     * @throws Exception if transport channels fail, timeouts hit, or structural hardware execution faults occur
     */
    public synchronized String execute(String command) throws Exception {
        if (!isConnected()) {
            throw new ET431Exception("Disconnected!: " + command);
        }

        flush();
        byte[] tx = (command + "\n").getBytes(StandardCharsets.US_ASCII);
        int written = port.writeBytes(tx, tx.length);
        if (written != tx.length) {
            throw new ET431Exception("Unable to send command: " + command);
        }

        Thread.sleep(100);
        int available = port.bytesAvailable();
        if (available <= 0) {
            return null;
        }

        byte[] buffer = new byte[available];
        port.readBytes(buffer, available);
        String response = new String(buffer, StandardCharsets.US_ASCII).trim();

        String cmdPart = String.format("[LCR command: %-30s]", command);
        String resPart = "[LCR response: " + response + "]";
        Log.d(cmdPart, resPart);

        if (response.equalsIgnoreCase("cmd err")) {
            throw new ET431Exception("Command error: " + command);
        }
        if (response.equalsIgnoreCase("exec err")) {
            throw new ET431Exception("Execution error: " + command);
        }

        return response;
    }

    /**
     * Clears out the serial port input buffer from any lingering or stale bytes.
     */
    private void flush() {
        byte[] buffer = new byte[256];
        while (port.bytesAvailable() > 0) {
            int n = Math.min(port.bytesAvailable(), buffer.length);
            port.readBytes(buffer, n);
        }
    }
}
package lcr.business;

import com.fazecast.jSerialComm.SerialPort;
import java.util.HashSet;
import java.util.Set;

public class SerialDetector {

    private Thread monitorThread;
    private volatile boolean isRunning = false;
    private final int scanIntervalMs;
    private OnPortDetectedListener listener;

    // DEJADO EXACTAMENTE IGUAL QUE ANTES
    public interface OnPortDetectedListener {
        void onNewPortConnected(String portName);
    }

    public SerialDetector(OnPortDetectedListener listener) {
        this(listener, 2000);
    }

    public SerialDetector(OnPortDetectedListener listener, int scanIntervalMs) {
        this.listener = listener;
        this.scanIntervalMs = scanIntervalMs;
    }

    public synchronized void startMonitoring() {
        if (isRunning) return;
        isRunning = true;

        monitorThread = new Thread(() -> {
            Set<String> previousPorts = getAvailablePortNames();
            System.out.println("Background serial port detector started (0% CPU impact)...");

            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(scanIntervalMs);

                    Set<String> currentPorts = getAvailablePortNames();

                    // Si la lista cambia por cualquier motivo (conectar o desconectar)
                    if (!currentPorts.equals(previousPorts)) {
                        System.out.println("\n[SYSTEM] Change detected in COM ports list.");

                        if (listener != null) {
                            // Disparamos tu listener idéntico para que refresque el ComboBox
                            listener.onNewPortConnected("");
                        }
                    }

                    previousPorts = currentPorts;

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    public synchronized void stopMonitoring() {
        isRunning = false;
        if (monitorThread != null) {
            monitorThread.interrupt();
        }
    }

    private Set<String> getAvailablePortNames() {
        Set<String> names = new HashSet<>();
        for (SerialPort port : SerialPort.getCommPorts()) {
            names.add(port.getSystemPortName());
        }
        return names;
    }
}